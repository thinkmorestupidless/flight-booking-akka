package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class FlightCommands extends AbstractActor {

    public static Props props(ActorRef flights, ActorRef readSide) {
        return Props.create(FlightCommands.class, () -> new FlightCommands(flights, readSide));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final ActorRef flights;

    private final ActorRef readSide;

    public FlightCommands(ActorRef flights, ActorRef readSide) {
        this.flights = flights;
        this.readSide = readSide;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(FlightCommand.AddFlight.class, cmd -> {
                    log.info("here i am");
                    flights.forward(cmd, getContext());
                })
                .match(ReadSideProtocol.ListFlights.class, cmd -> {
                    readSide.forward(cmd, getContext());
                })
                .matchAny(o -> log.warning("I don't know what to do with {}", o))
                .build();
    }
}
