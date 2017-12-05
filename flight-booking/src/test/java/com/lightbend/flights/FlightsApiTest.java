package com.lightbend.flights;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.testkit.JUnitRouteTest;
import akka.http.javadsl.testkit.TestRoute;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

public class FlightsApiTest extends JUnitRouteTest {

    ActorSystem system;

    Materializer materializer;

    @Before
    public void before() {
        system = ActorSystem.create();
        materializer = ActorMaterializer.create(system);
    }

    @After
    public void after() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testGetFlights() {

        TestRoute routes = testRoute(new FlightsApi(system.actorOf(Backend.props())).routes());

        routes.run(HttpRequest.GET("/flights"))
                .assertStatusCode(200)
                .assertContentType("application/json")
                .assertEntity("[]");
    }

    static class Backend extends AbstractActor {

        public static Props props() {
            return Props.create(Backend.class, () -> new Backend());
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder().match(ReadSideProtocol.ListFlights.class, this::listFlights).build();
        }

        private void listFlights(ReadSideProtocol.ListFlights cmd) {
            getSender().tell(Collections.emptyList(), getSelf());
        }
    }
}
