package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public interface FlightCommand extends Serializable {

    UUID getFlightId();

    @Value
    @JsonDeserialize
    final class AddFlight implements Serializable {

        public final String callsign;

        public final String equipment;

        public final String departureIata;

        public final String arrivalIata;

        @JsonCreator
        public AddFlight(@JsonProperty("callsign") String callsign,
                         @JsonProperty("equipment") String equipment,
                         @JsonProperty("departureIata") String departureIata,
                         @JsonProperty("arrivalIata") String arrivalIata) {
            this.callsign = callsign;
            this.equipment = equipment;
            this.departureIata = departureIata;
            this.arrivalIata = arrivalIata;
        }
    }

    @Value
    @JsonDeserialize
    final class AddPassenger implements FlightCommand {

        public final UUID flightId;

        public final UUID passengerId;

        public final String lastName;

        public final String firstName;

        public final String initial;

        public final Optional<String> seatAssignment;

        @JsonCreator
        public AddPassenger(@JsonProperty("flightId") UUID flightId,
                            @JsonProperty("passengerId") UUID passengerId,
                            @JsonProperty("lastName") String lastName,
                            @JsonProperty("firstName") String firstName,
                            @JsonProperty("initial") String initial,
                            @JsonProperty("seatAssignment") Optional<String> seatAssignment) {
            this.flightId = flightId;
            this.passengerId = passengerId;
            this.lastName = lastName;
            this.firstName = firstName;
            this.initial = initial;
            this.seatAssignment = seatAssignment;
        }
    }

    @Value
    @JsonDeserialize
    final class SelectSeat implements FlightCommand {

        public final UUID flightId;

        public final String passengerId;

        public final String seatAssignment;

        @JsonCreator
        public SelectSeat(@JsonProperty("flightId") UUID flightId,
                          @JsonProperty("passengerId") String passengerId,
                          @JsonProperty("seatAssignment") String seatAssignment) {
            this.flightId = flightId;
            this.passengerId = passengerId;
            this.seatAssignment = seatAssignment;
        }
    }

    @Value
    @JsonDeserialize
    final class RemovePassenger implements FlightCommand {

        public final UUID flightId;

        public final UUID passengerId;

        @JsonCreator
        public RemovePassenger(@JsonProperty("flightId") UUID flightId,
                               @JsonProperty("passengerId") UUID passengerId) {
            this.flightId = flightId;
            this.passengerId = passengerId;
        }
    }

    @Value
    @JsonDeserialize
    final class CloseFlight implements FlightCommand {

        public final UUID flightId;

        @JsonCreator
        public CloseFlight(@JsonProperty("flightId") UUID flightId) {
            this.flightId = flightId;
        }
    }
}
