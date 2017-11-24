package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

public interface FlightCommand extends Serializable {

    String getFlightId();

    @Value
    @JsonDeserialize
    final class AddFlight implements FlightCommand {

        public final String flightId;

        public final String callsign;

        public final String equipment;

        public final String departureIata;

        public final String arrivalIata;

        @JsonCreator
        public AddFlight(@JsonProperty("flightId") String flightId,
                         @JsonProperty("callsign") String callsign,
                         @JsonProperty("equipment") String equipment,
                         @JsonProperty("departureIata") String departureIata,
                         @JsonProperty("arrivalIata") String arrivalIata) {
            this.flightId = flightId;
            this.callsign = callsign;
            this.equipment = equipment;
            this.departureIata = departureIata;
            this.arrivalIata = arrivalIata;
        }
    }

    @Value
    @JsonDeserialize
    final class AddPassenger implements FlightCommand {

        public final String flightId;

        public final String passengerId;

        public final String lastName;

        public final String firstName;

        public final String initial;

        public final Optional<String> seatAssignment;

        @JsonCreator
        public AddPassenger(String flightId, String passengerId, String lastName, String firstName, String initial, Optional<String> seatAssignment) {
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

        public final String flightId;

        public final String passengerId;

        public final String seatAssignment;

        @JsonCreator
        public SelectSeat(String flightId, String passengerId, String seatAssignment) {
            this.flightId = flightId;
            this.passengerId = passengerId;
            this.seatAssignment = seatAssignment;
        }
    }

    @Value
    @JsonDeserialize
    final class RemovePassenger implements FlightCommand {

        public final String flightId;

        public final String passengerId;

        @JsonCreator
        public RemovePassenger(String flightId, String passengerId) {
            this.flightId = flightId;
            this.passengerId = passengerId;
        }
    }

    @Value
    @JsonDeserialize
    final class CloseFlight implements FlightCommand {

        public final String flightId;

        @JsonCreator
        public CloseFlight(String flightId) {
            this.flightId = flightId;
        }
    }
}
