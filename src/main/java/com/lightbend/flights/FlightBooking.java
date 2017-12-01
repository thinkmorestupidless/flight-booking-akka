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

public class FlightBooking {

    private final static Logger log = LoggerFactory.getLogger(FlightBooking.class);

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

            ActorRef readSide = system.actorOf(ReadSideSupervisor.props(new KafkaEventRegistrar(kafka)), "read-side-supervisor");

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
