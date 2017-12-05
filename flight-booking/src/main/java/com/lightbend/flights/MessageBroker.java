package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lightbend.kafka.KafkaActor;
import com.lightbend.kafka.KafkaProtocol;

public class MessageBroker extends AbstractActor {

    public static Props props(ActorRef broker) {
        return Props.create(MessageBroker.class, () -> new MessageBroker(broker));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef broker;

    public MessageBroker(ActorRef broker) {
        this.broker = broker;
    }

    @Override
    public void preStart() throws Exception {
        super.preStart();

        getContext().getSystem().eventStream().subscribe(getSelf(), FlightEvent.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FlightEvent.class, this::handleFlightEvent)
                .build();
    }

    public void handleFlightEvent(FlightEvent evt) {
        log.info("handling flight event -> {}", evt);

        if (evt instanceof FlightEvent.FlightAdded) {
            KafkaProtocol msg = KafkaProtocol.flightAdded(0L, evt);
            broker.tell(msg, getSelf());
        }
    }
}
