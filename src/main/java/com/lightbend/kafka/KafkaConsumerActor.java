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
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

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

    private final Map<String, Set<ActorRef>> subscribers = Maps.newHashMap();

    private SourceQueue<Object> queue;

    @Override
    public void preStart() throws Exception {

        Materializer materializer = ActorMaterializer.create(getContext());

        Consumer.committableSource(ConsumerSettings.create(getContext().getSystem(), new ByteArrayDeserializer(), new StringDeserializer()), Subscriptions.topics("flights"))
                .mapAsync(1, this::handleMessage)
                .batch(20,
                        first -> ConsumerMessage.emptyCommittableOffsetBatch().updated(first),
                        (batch, elem) -> batch.updated(elem))
                .mapAsync(3, c -> c.commitJavadsl())
                .runWith(Sink.ignore(), materializer);
    }

    private CompletionStage<ConsumerMessage.CommittableOffset> handleMessage(ConsumerMessage.CommittableMessage<byte[], String> msg) {
        return CompletableFuture.completedFuture(msg.committableOffset());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaProtocol.SubscribeToTopic.class, this::subscribe)
                .build();
    }

    public void subscribe(KafkaProtocol.SubscribeToTopic subscription) {
        if (!subscribers.containsKey(subscription.getTopic())) {
            subscribers.put(subscription.getTopic(), Sets.newHashSet());
        }

        subscribers.get(subscription.getTopic()).add(getSender());
    }
}
