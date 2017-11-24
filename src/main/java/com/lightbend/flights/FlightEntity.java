package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;

public class FlightEntity extends AbstractPersistentActor {

    public static final Props props() {
        return Props.create(FlightEntity.class, () -> new FlightEntity());
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private FlightState state;

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(RecoveryCompleted.class, m -> log.info("recovery completed"))
                .matchAny(o -> log.warning("I don't know what to do with {}", o)).build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FlightCommand.AddFlight.class, cmd -> {
                    log.info("Here i am finally");
                    addFlight(cmd);
                })
                .matchAny(o -> log.warning("I don't know what to do with {}", o)).build();
    }

    public void addFlight(FlightCommand.AddFlight cmd) {
        log.info("adding flight -> {}", cmd);

        ActorRef sender = getSender();

        FlightEvent evt = new FlightEvent.FlightAdded(cmd.flightId, cmd.callsign, cmd.equipment, cmd.departureIata, cmd.arrivalIata);

        persist(evt, e -> {
            state = FlightState.empty();
            sender.tell(e, getSelf());
            getContext().getSystem().eventStream().publish(e);

            log.info("event {} created", persistenceId());
        });
    }

    @Override
    public String persistenceId() {
        return getSelf().path().name();
    }
}
