package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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

            ActorRef flights = system.actorOf(Flights.props(), "flights");
            ActorRef readSide = system.actorOf(ReadSideSupervisor.props(), "read-side");

            ActorRef commands = system.actorOf(FlightCommands.props(flights, readSide), "flight-commands");

            readSide.tell(new ReadSideProtocol.Start(), ActorRef.noSender());

            final int port = 8080 + i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Api api = new Api(commands);
                    try {
                        api.startServer("localhost", port);
                    } catch (Exception e) {

                    }
                }
            }).start();
        }
    }

    static class Api extends HttpApp {

        private final static Logger log = LoggerFactory.getLogger(Api.class);

        private final ActorRef backend;

        public Api(ActorRef backend) {
            this.backend = backend;
        }

        @Override
        protected Route routes() {
            return path("flights", () ->
                route(post(() ->
                        entity(Jackson.unmarshaller(FlightCommand.AddFlight.class), cmd -> {
                            Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

                            CompletionStage<Object> response = PatternsCS.ask(backend, cmd, timeout);

                            return completeOKWithFuture(response, Jackson.marshaller());
                        })),
                    get(() -> {
                        Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

                        CompletionStage<Object> response = PatternsCS.ask(backend, new ReadSideProtocol.ListFlights(), timeout);

                        return completeOKWithFuture(response, Jackson.marshaller());
                    })));
        }
    }
}
