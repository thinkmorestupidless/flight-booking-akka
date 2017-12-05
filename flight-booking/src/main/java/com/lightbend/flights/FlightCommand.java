package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.*;

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
            checkNotNull(this.callsign = callsign);
            checkNotNull(this.equipment = equipment);
            checkNotNull(this.departureIata = departureIata);
            checkNotNull(this.arrivalIata = arrivalIata);
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
            checkNotNull(this.flightId = flightId);
            checkNotNull(this.passengerId = passengerId);
            checkNotNull(this.lastName = lastName);
            checkNotNull(this.firstName = firstName);
            this.initial = initial;
            this.seatAssignment = seatAssignment;
        }
    }

    @Value
    @JsonDeserialize
    final class SelectSeat implements FlightCommand {

        public final UUID flightId;

        public final UUID passengerId;

        public final String seatAssignment;

        @JsonCreator
        public SelectSeat(@JsonProperty("flightId") UUID flightId,
                          @JsonProperty("passengerId") UUID passengerId,
                          @JsonProperty("seatAssignment") String seatAssignment) {
            checkNotNull(this.flightId = flightId);
            checkNotNull(this.passengerId = passengerId);
            checkNotNull(this.seatAssignment = seatAssignment);
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
            checkNotNull(this.flightId = flightId);
            checkNotNull(this.passengerId = passengerId);
        }
    }

    @Value
    @JsonDeserialize
    final class CloseFlight implements FlightCommand {

        public final UUID flightId;

        @JsonCreator
        public CloseFlight(@JsonProperty("flightId") UUID flightId) {
            checkNotNull(this.flightId = flightId);
        }
    }
}
