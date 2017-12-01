package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
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
import com.lightbend.cassandra.CassandraSession;
import com.lightbend.kafka.KafkaProtocol;

/**
 * Handles the events dispatched from the write side.
 * At the moment it just adds flights.
 */
public class ReadSideEventProcessor extends AbstractActor {

    public static Props props(CassandraSession session, EventRegistrar registrar) {
        return Props.create(ReadSideEventProcessor.class, () -> new ReadSideEventProcessor(session, registrar));
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private final CassandraSession session;

    private final EventRegistrar registrar;

    private final Receive receiveEvents = receiveBuilder().match(FlightEvent.FlightAdded.class, this::flightAdded)
                                                          .matchAny(o -> log.warning("i don't know what to do with {}", o))
                                                          .build();

    private PreparedStatement insertEventStatement;

    public ReadSideEventProcessor(CassandraSession session, EventRegistrar registrar) {
        this.session = session;
        this.registrar = registrar;
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
        getContext().become(receiveEvents);

        registrar.register(getSelf());
    }

    public void flightAdded(FlightEvent.FlightAdded evt) {
        log.info("creating event -> {}", evt);

        session.execute(insertEventStatement.bind(evt.flightId, evt.callsign, evt.equipment, evt.departureIata, evt.arrivalIata));
    }
}
