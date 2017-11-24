package com.lightbend.flights;

import com.datastax.driver.core.Row;
import lombok.Value;

@Value
public class Flight {

    private final String flightId;

    private final String callsign;

    private final String equipment;

    private final String departureIata;

    private final String arrivalIata;

    public static Flight create(Row row) {
        return new Flight(row.getString("flightId"), row.getString("callsign"), row.getString("equipment"), row.getString("departureIata"), row.getString("arrivalIata"));
    }
}
