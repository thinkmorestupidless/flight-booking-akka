package com.lightbend.flights;

import akka.http.javadsl.marshallers.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JacksonTest {

    @Test
    public void testUnmarshal() throws Exception {
        FlightCommand addFlight = new FlightCommand.AddFlight("a", "b", "c", "d", "e");
        ObjectMapper mapper = new ObjectMapper();

        String s = mapper.writeValueAsString(addFlight);

        FlightCommand f = mapper.readValue(s, FlightCommand.AddFlight.class);
    }
}
