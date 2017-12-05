package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FlightEntityTest {

    ActorSystem system;

    @Before
    public void before() {
        system = ActorSystem.create();
    }

    public void after() {
        TestKit.shutdownActorSystem(system);
    }

    @Test
    public void testAddFlight() {

        new TestKit(system) {{
            UUID persistenceId = UUID.randomUUID();

            ActorRef actor = system.actorOf(FlightEntity.props(), persistenceId.toString());

            within(duration("10 seconds"), () -> {
                actor.tell(new FlightCommand.AddFlight("a", "b", "c", "d"), getRef());

                FlightEvent evt = new FlightEvent.FlightAdded(persistenceId, "a", "b", "c", "d");

                expectMsg(FiniteDuration.apply(10, TimeUnit.SECONDS), evt);

                return null;
            });

        }};
    }
}
