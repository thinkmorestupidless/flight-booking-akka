package com.lightbend.flights;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

@Value
@JsonDeserialize
public class FlightInfo {

    public final String flightId;
    public final String callsign;
    public final String equipment;
    public final String departureIata;
    public final String arrivalIata;
    public final Boolean doorsClosed;

    @JsonCreator
    public FlightInfo(String flightId, String callsign, String equipment, String departureIata, String arrivalIata, Boolean doorsClosed) {
        this.flightId = flightId;
        this.callsign = callsign;
        this.equipment = equipment;
        this.departureIata = departureIata;
        this.arrivalIata = arrivalIata;
        this.doorsClosed = doorsClosed;
    }

    public FlightInfo withDoorsClosed(Boolean doorsClosed) {
        return new FlightInfo(flightId, callsign, equipment, departureIata, arrivalIata, doorsClosed);
    }
}
