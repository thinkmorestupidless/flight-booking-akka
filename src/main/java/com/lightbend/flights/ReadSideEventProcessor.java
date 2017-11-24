package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.datastax.driver.core.PreparedStatement;

import java.util.UUID;

public class ReadSideEventProcessor extends AbstractActor {

    public static Props props(CassandraSession session) {
        return Props.create(ReadSideEventProcessor.class, () -> new ReadSideEventProcessor(session));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final CassandraSession session;

    private PreparedStatement insertEventStatement;

    public ReadSideEventProcessor(CassandraSession session) {
        this.session = session;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ReadSideProtocol.Start.class, this::start)
                .match(ReadSideProtocol.RegisterForEvents.class, this::registerForEvents)
                .build();
    }

    public void start(ReadSideProtocol.Start cmd) {
        prepareStatements();
    }

    public void prepareStatements() {
        session.prepare("INSERT INTO flights.flights (flightId, callsign, equipment, departureIata, arrivalIata) VALUES (?,?,?,?,?)")
                .exceptionally(t -> {
                    log.error("failed to create insertEventStatement -> {}", t);
                    return null;
                })
                .thenAccept(statement -> {
                    this.insertEventStatement = statement;
                    getSelf().tell(new ReadSideProtocol.RegisterForEvents(), getSelf());
                });
    }

    public void registerForEvents(ReadSideProtocol.RegisterForEvents cmd) {
        log.info("registering for events");

        Materializer materializer = ActorMaterializer.create(getContext().getSystem());

        CassandraReadJournal journal = PersistenceQuery.get(getContext().getSystem())
                .getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());

        journal.eventsByTag("event", Offset.noOffset()).runForeach(this::handleEvent, materializer);
    }

    public void handleEvent(EventEnvelope evt) {
        log.info("handling event {}", evt.event());

        if (evt.event() instanceof FlightEvent.FlightAdded) {
            createEvent((FlightEvent.FlightAdded) evt.event());
        }
    }

    public void createEvent(FlightEvent.FlightAdded evt) {
        log.info("creating event -> {}", evt);

        session.execute(insertEventStatement.bind(cleanEventId(evt.flightId), evt.callsign, evt.equipment, evt.departureIata, evt.arrivalIata));
    }

    public UUID cleanEventId(String eventId) {
        if (eventId.startsWith("flight-")) {
            return UUID.fromString(eventId.substring("flight-".length()));
        }

        return UUID.fromString(eventId);
    }
}
