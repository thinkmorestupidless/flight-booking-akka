package com.lightbend.flights;

import akka.actor.ActorRef;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.util.Timeout;
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

public class FlightsApi extends AllDirectives {

    private final static Logger log = LoggerFactory.getLogger(FlightsApi.class);

    private final ActorRef backend;

    public FlightsApi(ActorRef backend) {
        this.backend = backend;
    }

    /**
     * HTTP routes into this application:
     *
     * POST     /flights                            AddFlight
     * GET      /flights                            ListFlights
     * DELETE   /flights/:flightId                  CloseFlight(flightId)
     * POST     /passengers                         AddPassenger
     * PUT      /passengers                         SelectSeat
     * DELETE   /passengers/:flightId/:passengerId  RemovePassenger(flightId, passengerId)
     *
     * @return
     */
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
                        delete(() -> execute(new FlightCommand.RemovePassenger(flightId, passengerId)))
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
