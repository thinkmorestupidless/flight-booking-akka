package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import com.lightbend.kafka.KafkaActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple microservice implementation using Akka.
 *
 * Receives HTTP requests and passes them to Actors which do the work and respond.
 * Implements CQRS - Persistent Entities emit events which are observed by the read-side.
 *
 * Change the 'CQRS' setting to switch between using Akka's built-in 'persistence-query' module (with VIA.AKKA) or
 * Kafka (with VIA.KAFKA) to provide the communication between write and read sides.
 *
 * (NOTE: If you're using Kafka for passing events to the read-side then make sure you have a Kafka broker running!).
 */
public class FlightBooking {

    public enum VIA {
        AKKA, KAFKA
    }

    private final static Logger log = LoggerFactory.getLogger(FlightBooking.class);

    private final static VIA CQRS = VIA.KAFKA;

    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            startup(new String[] { "3551", "3552", "0" });
        else
            startup(args);
    }

    public static void startup(String[] ports) throws Exception {
        for (int i = 0; i < ports.length; i++) {

            Config config = ConfigFactory.parseString(
                    "akka.remote.netty.tcp.port=" + ports[i]).withFallback(
                    ConfigFactory.load());

            ActorSystem system = ActorSystem.create("FlightBooking", config);
            Materializer materializer = ActorMaterializer.create(system);

            ActorRef flights = system.actorOf(Flights.props(), "flights");

            ActorRef kafka = system.actorOf(KafkaActor.props(), "kafka");

            ActorRef broker = system.actorOf(MessageBroker.props(kafka), "message-broker");

            EventRegistrar registrar = CQRS == VIA.AKKA ? new PersistenceQueryEventRegistrar(system) : new KafkaEventRegistrar(kafka);

            ActorRef readSide = system.actorOf(ReadSideSupervisor.props(registrar), "read-side-supervisor");

            ActorRef commands = system.actorOf(FlightCommands.props(flights, readSide), "flight-commands");

            readSide.tell(new ReadSideProtocol.Start(), ActorRef.noSender());

            final int port = 8080 + i;

            FlightsApi api = new FlightsApi(commands);

            Http.get(system)
                .bindAndHandle(api.routes()
                                  .flow(system, materializer),
                               ConnectHttp.toHost("localhost", port),
                               materializer);
        }
    }
}
