/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The state for the {@link FlightEntity} entity.
 */
@Value
@JsonDeserialize
public final class FlightState {

  public final Optional<FlightInfo> flightInfo;

  public final Set<Passenger> passengers;

  @JsonCreator
  public FlightState(Optional<FlightInfo> flightInfo, Set<Passenger> passengers) {
    this.flightInfo = flightInfo;
    this.passengers = passengers;
  }

  public static FlightState empty() {
    return new FlightState(Optional.empty(), Collections.emptySet());
  }

  public FlightState withPassenger(Passenger passenger) {
    Set<Passenger> copy = new HashSet<>(passengers);
    copy.add(passenger);

    return new FlightState(flightInfo, copy);
  }

  public FlightState updatePassenger(Passenger passenger) {
    Set<Passenger> updated = passengers.stream().filter(p -> !p.passengerId.equals(passenger.passengerId)).collect(Collectors.toSet());
    updated.add(passenger);

    return new FlightState(flightInfo, updated);
  }

  public FlightState withoutPassenger(String passengerId) {
    return new FlightState(flightInfo, passengers.stream().filter(p -> !p.passengerId.equals(passengerId)).collect(Collectors.toSet()));
  }

  public FlightState withDoorsClosed(Boolean doorsClosed) {
    return new FlightState(Optional.of(flightInfo.get().withDoorsClosed(doorsClosed)), passengers);
  }
}
