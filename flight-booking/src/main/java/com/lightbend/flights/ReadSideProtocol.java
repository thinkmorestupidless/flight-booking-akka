package com.lightbend.flights;

/**
 * Defines the messages that can be sent to the read side.
 */
public interface ReadSideProtocol {

    class Start implements ReadSideProtocol {}

    class RegisterForEvents implements ReadSideProtocol {}

    class ListFlights implements ReadSideProtocol {}

    static ReadSideProtocol listFlights() {
        return new ListFlights();
    }
}
