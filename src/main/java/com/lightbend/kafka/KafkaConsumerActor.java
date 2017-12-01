package com.lightbend.kafka;

import akka.Done;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.SourceQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lightbend.flights.FlightEvent;
import lombok.Value;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class KafkaConsumerActor extends AbstractActor {

    public static Props props() {
        return Props.create(KafkaConsumerActor.class, KafkaConsumerActor::new);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Subscription> subscriptions = Maps.newHashMap();

    private Materializer materializer;

    private SourceQueue<Object> queue;

    @Override
    public void preStart() throws Exception {
        materializer = ActorMaterializer.create(getContext());
    }

    private CompletionStage<ConsumerMessage.CommittableOffset> flightAdded(ConsumerMessage.CommittableMessage<byte[], String> msg) throws IOException {
        log.info("handling flight-added -> {}", msg.record().value());

        return handleMessage("flight-added", msg);
    }

    private CompletionStage<ConsumerMessage.CommittableOffset> handleMessage(String topic, ConsumerMessage.CommittableMessage<byte[], String> msg) throws IOException {
        log.info("handling message {} with {}", msg, subscriptions);

        if (subscriptions.containsKey(topic)) {
            Subscription subscription = subscriptions.get(topic);

            log.info("subscription is {}", subscription);

            FlightEvent evt = mapper.readValue(msg.record().value(), FlightEvent.class);

            log.info("deserialized event -> {}", evt);

            subscription.getSubscribers().forEach(subscriber ->
            {
                log.info("sending {} to {}", evt, subscriber);
                subscriber.tell(evt, getSelf());
            });
        }

        return CompletableFuture.completedFuture(msg.committableOffset());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaProtocol.SubscribeToTopic.class, this::subscribe)
                .build();
    }

    public void subscribe(KafkaProtocol.SubscribeToTopic subscription) {
        log.info("subscribing {} to topic -> {}", getSender(), subscription);

        ActorRef sender = getSender();

        if (!subscriptions.containsKey(subscription.getTopic())) {
            CompletionStage<Done> stream = Consumer.committableSource(ConsumerSettings.create(getContext().getSystem(), new ByteArrayDeserializer(), new StringDeserializer()).withGroupId("flight-booking"), Subscriptions.topics(subscription.getTopic()))
                    .mapAsync(1, this::flightAdded)
                    .batch(20,
                            first -> ConsumerMessage.emptyCommittableOffsetBatch().updated(first),
                            (batch, elem) -> batch.updated(elem))
                    .mapAsync(3, c -> c.commitJavadsl())
                    .runWith(Sink.ignore(), materializer);

            subscriptions.put(subscription.getTopic(), new Subscription(stream));
        }

        subscriptions.get(subscription.getTopic()).getSubscribers().add(sender);
    }

    @Value
    public class Subscription {

        private final CompletionStage<Done> stream;

        private final Set<ActorRef> subscribers;

        public Subscription(CompletionStage<Done> stream) {
            this.stream = stream;

            subscribers = Sets.newHashSet();
        }

        public Set<ActorRef> getSubscribers() {
            return subscribers;
        }
    }
}
