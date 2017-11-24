package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;

public interface FlightEvent extends Serializable {

    @Value
    @JsonDeserialize
    final class FlightAdded implements FlightEvent {

        public final String flightId;

        public final String callsign;

        public final String equipment;

        public final String departureIata;

        public final String arrivalIata;

        @JsonCreator
        public FlightAdded(String flightId, String callsign, String equipment, String departureIata, String arrivalIata) {
            this.flightId = flightId;
            this.callsign = callsign;
            this.equipment = equipment;
            this.departureIata = departureIata;
            this.arrivalIata = arrivalIata;
        }
    }

    @Value
    @JsonDeserialize
    final class PassengerAdded implements FlightEvent {

        public final String flightId;

        public final String passengerId;

        public final String lastName;

        public final String firstName;

        public final String initial;

        public final Optional<String> seatAssignment;

        @JsonCreator
        public PassengerAdded(String flightId, String passengerId, String lastName, String firstName, String initial, Optional<String> seatAssignment) {
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
    final class SeatSelected implements FlightEvent {

        public final String flightId;

        public final String passengerId;

        public final String seatAssignment;

        @JsonCreator
        public SeatSelected(String flightId, String passengerId, String seatAssignment) {
            this.flightId = flightId;
            this.passengerId = passengerId;
            this.seatAssignment = seatAssignment;
        }
    }

    @Value
    @JsonDeserialize
    final class PassengerRemoved implements FlightEvent {

        public final String flightId;

        public final String passengerId;

        @JsonCreator
        public PassengerRemoved(String flightId, String passengerId) {
            this.flightId = flightId;
            this.passengerId = passengerId;
        }
    }

    @Value
    @JsonDeserialize
    final class FlightClosed implements FlightEvent {

        public final String flightId;

        @JsonCreator
        public FlightClosed(String flightId) {
            this.flightId = flightId;
        }
    }
}
