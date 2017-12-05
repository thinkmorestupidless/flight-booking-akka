package com.lightbend.kafka;

import com.lightbend.flights.FlightEvent;
import lombok.Value;

public interface KafkaProtocol {

    String getTopic();

    @Value
    class SendMessage<T, K> implements KafkaProtocol {

        private final String topic;

        private final T key;

        private final K message;
    }

    static SendMessage<Long, FlightEvent> flightAdded(Long key, FlightEvent message) {
        return new SendMessage("flight-added", key, message);
    }

    @Value
    class SubscribeToTopic implements KafkaProtocol {

        private final String topic;
    }
}
