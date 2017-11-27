package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.util.Timeout;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.http.javadsl.marshallers.jackson.Jackson.marshaller;
import static akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.PathMatchers.uuidSegment;
import static akka.pattern.PatternsCS.ask;
import static com.lightbend.flights.ReadSideProtocol.listFlights;

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

            // TODO: is this really necessary?
            // If i put the server inside an Actor i can't get message replies
            // If i don't put it in a Thread then it blocks at this point and never
            // continues to the next iteration.
            new Thread(() -> {
                try {
                    new Api(commands).startServer("localhost", port);
                } catch (Exception e) {
                    log.error("error starting HTTP server", e);
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
            return route(
                        path("flights", () ->
                            route(
                                post(() -> entity(unmarshaller(FlightCommand.AddFlight.class), cmd -> execute(cmd))),
                                get(() -> execute(listFlights()))
                            )
                        ),
                        path(segment("flights").slash(uuidSegment()), flightId ->
                            delete(() -> execute(new FlightCommand.CloseFlight(flightId)))
                        ),
                        path("passengers", () ->
                            route(
                                post(() -> entity(unmarshaller(FlightCommand.AddPassenger.class), cmd -> execute(cmd))),
                                put(() -> entity(unmarshaller(FlightCommand.SelectSeat.class), cmd -> execute(cmd)))
                            )
                        ),
                        path(segment("passengers").slash(uuidSegment()).slash(uuidSegment()), (flightId, passengerId) ->
                            put(() -> execute(new FlightCommand.RemovePassenger(flightId, passengerId)))
                        )
                    );
        }

        private RouteAdapter execute(Object o) {
            Timeout timeout = new Timeout(Duration.create(5, TimeUnit.SECONDS));

            return completeOKWithFuture(ask(backend, o, timeout), marshaller());
        }

        private RouteAdapter execute(Object o, Timeout timeout) {
            return completeOKWithFuture(ask(backend, o, timeout), marshaller());
        }
    }
}
