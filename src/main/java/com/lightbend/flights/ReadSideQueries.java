package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ReadSideQueries extends AbstractActor {

    public static Props props(CassandraSession session) {
        return Props.create(ReadSideQueries.class, () -> new ReadSideQueries(session));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final CassandraSession session;

    public ReadSideQueries(CassandraSession session) {
        this.session = session;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReadSideProtocol.ListFlights.class, this::listFlights)
                .build();
    }

    public void listFlights(ReadSideProtocol.ListFlights query) {
        ActorRef sender = getSender();

        session.execute("SELECT * FROM flights.flights")
                .exceptionally(t -> {
                    log.warning("failed to retrieve flights -> {}", t);
                    return Collections.emptyList();
                })
                .thenAccept(result -> {
                    List<Flight> events = result.stream()
                            .map(Flight::create)
                            .collect(Collectors.toList());

                    sender.tell(events, getSelf());
                });
    }
}
