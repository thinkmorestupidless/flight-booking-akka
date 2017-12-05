package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.Offset;
import akka.persistence.query.PersistenceQuery;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;

public class PersistenceQueryEventRegistrar implements EventRegistrar {

    private final ActorSystem system;

    private ActorRef actor;

    public PersistenceQueryEventRegistrar(ActorSystem system) {
        this.system = system;
    }

    @Override
    public void register(ActorRef actor) {
        Materializer materializer = ActorMaterializer.create(system);

        PersistenceQuery.get(system)
                .getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier())
                .eventsByTag("flight", Offset.noOffset()).runForeach(this::handleEvent, materializer);
    }

    public void handleEvent(EventEnvelope env) {
        if (env.event() instanceof FlightEvent) {
            actor.tell(env.event(), actor);
        }
    }
}
