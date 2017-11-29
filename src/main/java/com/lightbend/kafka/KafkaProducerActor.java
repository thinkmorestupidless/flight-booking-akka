package com.lightbend.kafka;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.kafka.ProducerSettings;
import akka.kafka.javadsl.Producer;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducerActor extends AbstractActor {

    public static Props props() {
        return Props.create(KafkaProducerActor.class, KafkaProducerActor::new);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ObjectMapper mapper = new ObjectMapper();

    private SourceQueue<Object> queue;

    @Override
    public void preStart() throws Exception {

        queue = Source.queue(100, OverflowStrategy.backpressure())
                .map(o -> mapper.writeValueAsString(o))
                .map(o -> new ProducerRecord<byte[], String>("flights", o))
                .to(Producer.plainSink(ProducerSettings.create(getContext().getSystem(), new ByteArraySerializer(), new StringSerializer())))
                .run(ActorMaterializer.create(getContext()));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaProtocol.SendMessage.class, this::sendMessage)
                .matchAny(o -> log.info("i don't know what to do with -> {}", o))
                .build();
    }

    public void sendMessage(KafkaProtocol.SendMessage m) {
        log.info("sending message -> {}", m);

        queue.offer(m.getMessage());
    }
}
