package com.lightbend.flights;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lightbend.cassandra.CassandraSession;
import com.lightbend.cassandra.CassandraSessionImpl;

import java.util.Collections;

/**
 * Supervises the read side.
 * Read side is made up of the Actor that handles queries and the Actor that handles the events from the write side.
 */
public class ReadSideSupervisor extends AbstractActorWithStash {

    public static Props props() {
        return Props.create(ReadSideSupervisor.class, ReadSideSupervisor::new);
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private CassandraSession session;

    private ActorRef queries;

    @Override
    public Receive createReceive() {
        return initialBehaviour;
    }

    private Receive initialBehaviour = receiveBuilder()
            .match(ReadSideProtocol.ListFlights.class, e -> stash())
            .match(ReadSideProtocol.Start.class, this::start)
            .match(ReadSideProtocol.RegisterForEvents.class, this::registerForEvents)
            .build();

    private Receive ableToHandleQueries = receiveBuilder()
            .match(ReadSideProtocol.ListFlights.class, e -> queries.forward(e, getContext()))
            .build();

    public void registerForEvents(ReadSideProtocol.RegisterForEvents cmd) {
        log.debug("read side registering for events");

        getContext().actorOf(ReadSideEventProcessor.props(session), "read-side-events").tell(new ReadSideProtocol.Start(), getSelf());

        queries = getContext().actorOf(ReadSideQueries.props(session), "read-side-queries");
        queries.tell(new ReadSideProtocol.Start(), getSelf());

        unstashAll();
        getContext().become(ableToHandleQueries);
    }

    public void start(ReadSideProtocol.Start cmd) {
        log.debug("starting read side");

        (session = new CassandraSessionImpl()).connect(getContext().getSystem(), getContext().dispatcher()).thenAccept(done -> prepareKeyspace());
    }

    public void prepareKeyspace() {

        session.execute("CREATE KEYSPACE IF NOT EXISTS flights " +
                                "WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} " +
                                "AND durable_writes = true")
                .exceptionally(t -> {
                    log.error("failed to create schema -> {}", t);
                    return Collections.emptyList();
                })
                .thenAccept(result -> prepareTables());
    }

    public void prepareTables() {

        session.execute("CREATE TABLE IF NOT EXISTS flights.flights (" +
                                "flightId UUID, " +
                                "callsign text, " +
                                "equipment text, " +
                                "departureIata text, " +
                                "arrivalIata text, " +
                                "PRIMARY KEY (flightId)" +
                                ")")
                .exceptionally(t -> {
                    log.error("failed to create tables -> {}", t);
                    return Collections.emptyList();
                })
                .thenAccept(result -> getSelf().tell(new ReadSideProtocol.RegisterForEvents(), getSelf()));
    }
}
