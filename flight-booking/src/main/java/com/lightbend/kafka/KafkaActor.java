package com.lightbend.kafka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class KafkaActor extends AbstractActor {

    public static Props props() {
        return Props.create(KafkaActor.class, KafkaActor::new);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ActorRef producer;

    private ActorRef consumer;

    @Override
    public void preStart() throws Exception {
        producer = getContext().actorOf(KafkaProducerActor.props(), "kafka-producer");
        consumer = getContext().actorOf(KafkaConsumerActor.props(), "kafka-consumer");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(KafkaProtocol.SendMessage.class, m -> producer.forward(m, getContext()))
                .match(KafkaProtocol.SubscribeToTopic.class, m -> consumer.forward(m, getContext()))
                .build();
    }
}
