package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.util.Optional;

@Value
@JsonDeserialize
public class Passenger {

    public final String passengerId;

    public final String lastName;

    public final String firstName;

    public final String initial;

    public final Optional<String> seatAssignment;

    @JsonCreator
    public Passenger(String passengerId, String lastName, String firstName, String initial, Optional<String> seatAssignment) {
        this.passengerId = passengerId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.initial = initial;
        this.seatAssignment = seatAssignment;
    }

    public Passenger withSeatAssignment(Optional<String> seatAssignment) {
        return new Passenger(passengerId, lastName, firstName, initial, seatAssignment);
    }
}
