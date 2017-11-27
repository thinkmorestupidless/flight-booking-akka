package com.lightbend.flights;

public interface ReadSideProtocol {

    class Start {}

    class RegisterForEvents {}

    class ListFlights implements ReadSideProtocol {}

    static ReadSideProtocol listFlights() {
        return new ListFlights();
    }
}
