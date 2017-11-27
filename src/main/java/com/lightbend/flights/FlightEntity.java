package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;
import akka.persistence.RecoveryCompleted;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class FlightEntity extends AbstractPersistentActor {

    public static final Props props() {
        return Props.create(FlightEntity.class, () -> new FlightEntity());
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private FlightState state;

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(FlightEvent.FlightAdded.class, evt -> state = new FlightState(Optional.of(new FlightInfo(UUID.fromString(persistenceId()), evt.callsign, evt.equipment, evt.departureIata, evt.arrivalIata, false)), Collections.emptySet()))
                .match(FlightEvent.PassengerAdded.class, evt -> state = state.withPassenger(new Passenger(evt.passengerId, evt.lastName, evt.firstName, evt.initial, evt.seatAssignment)))
                .match(FlightEvent.SeatSelected.class, evt -> state = state.updatePassenger(state.passengers.stream()
                                                                           .filter(p -> p.passengerId.equals(evt.passengerId))
                                                                           .findFirst()
                                                                           .orElseThrow(() -> new RuntimeException(String.format("passenger %s does not exist!", evt.passengerId)))
                                                                           .withSeatAssignment(Optional.ofNullable(evt.seatAssignment))))
                .match(FlightCommand.RemovePassenger.class, evt -> state = state.withoutPassenger(evt.passengerId))
                .match(FlightCommand.CloseFlight.class, evt -> state = state.withDoorsClosed(true))
                .match(RecoveryCompleted.class, m -> log.info("recovery completed"))
                .matchAny(o -> log.warning("I don't know what to do with {}", o)).build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FlightCommand.AddFlight.class, this::addFlight)
                .match(FlightCommand.AddPassenger.class, this::addPassenger)
                .match(FlightCommand.SelectSeat.class, this::selectSeat)
                .match(FlightCommand.RemovePassenger.class, this::removePassenger)
                .match(FlightCommand.CloseFlight.class, this::closeFlight)
                .matchAny(o -> log.warning("I don't know what to do with {}", o)).build();
    }

    public void addFlight(FlightCommand.AddFlight cmd) {
        log.debug("adding flight -> {}", cmd);

        ActorRef sender = getSender();

        FlightEvent.FlightAdded evt = new FlightEvent.FlightAdded(persistenceUUID(), cmd.callsign, cmd.equipment, cmd.departureIata, cmd.arrivalIata);

        persist(evt, e -> {
            state = new FlightState(Optional.of(new FlightInfo(persistenceUUID(), e.callsign, e.equipment, e.departureIata, e.arrivalIata, false)), Collections.emptySet());
            replyAndPublish(e, sender);

            log.debug("event {} created", e);
        });
    }

    public void addPassenger(FlightCommand.AddPassenger cmd) {
        log.debug("adding passenger -> {}", cmd);

        ActorRef sender = getSender();

        if (cmd.passengerId == null) {
            sender.tell("failed", getSelf());
        }

        FlightEvent.PassengerAdded evt = new FlightEvent.PassengerAdded(persistenceUUID(), cmd.passengerId, cmd.lastName, cmd.firstName, cmd.initial, cmd.seatAssignment);

        persist(evt, e -> {

            state = state.withPassenger(new Passenger(e.passengerId, e.lastName, e.firstName, e.initial, e.seatAssignment));
            replyAndPublish(e, sender);

            log.debug("event {} created", e);
        });
    }

    public void selectSeat(FlightCommand.SelectSeat cmd) {
        log.debug("selecting seat -> {}", cmd);

        ActorRef sender = getSender();

        FlightEvent.SeatSelected evt = new FlightEvent.SeatSelected(persistenceUUID(), cmd.passengerId, cmd.seatAssignment);

        persist(evt, e -> {
            Passenger passenger = state.passengers.stream()
                                       .filter(p -> p.passengerId.equals(evt.passengerId))
                                       .findFirst()
                                       .orElseThrow(() -> new RuntimeException(String.format("passenger %s does not exist!", evt.passengerId)))
                                       .withSeatAssignment(Optional.ofNullable(evt.seatAssignment));

            state = state.updatePassenger(passenger);
            replyAndPublish(e, sender);

            log.debug("event {} created", e);
        });
    }

    public void removePassenger(FlightCommand.RemovePassenger cmd) {
        log.debug("removing passenger -> {}", cmd);

        ActorRef sender = getSender();

        FlightEvent.PassengerRemoved evt = new FlightEvent.PassengerRemoved(persistenceUUID(), cmd.passengerId);

        persist(evt, e -> {
            state = state.withoutPassenger(cmd.passengerId);
            replyAndPublish(e, sender);

            log.debug("event {} created", e);
        });
    }

    public void closeFlight(FlightCommand.CloseFlight cmd) {
        log.debug("closing flight -> {}", cmd);

        ActorRef sender = getSender();

        FlightEvent.FlightClosed evt = new FlightEvent.FlightClosed(persistenceUUID());

        persist(evt, e -> {
           state = state.withDoorsClosed(true);
           replyAndPublish(e, sender);

           log.debug("event {} created", e);
        });
    }

    private void replyAndPublish(FlightEvent e, ActorRef actor) {
        reply(e, actor);
        publish(e);
    }

    private void reply(FlightEvent e, ActorRef actor) {
        actor.tell(e, getSelf());
    }

    private void publish(FlightEvent e) {
        getContext().getSystem().eventStream().publish(e);
    }

    @Override
    public String persistenceId() {
        return getSelf().path().name();
    }

    private UUID persistenceUUID() {
        return UUID.fromString(persistenceId());
    }
}
