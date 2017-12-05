package com.lightbend.flights;

import akka.actor.ActorRef;
import com.lightbend.kafka.KafkaProtocol;

public class KafkaEventRegistrar implements EventRegistrar {

    private final ActorRef kafka;

    public KafkaEventRegistrar(ActorRef kafka) {
        this.kafka = kafka;
    }

    @Override
    public void register(ActorRef actor) {
        kafka.tell(new KafkaProtocol.SubscribeToTopic("flight-added"), actor);
    }
}
