package com.lightbend.kafka;

import lombok.Value;

public interface KafkaProtocol {

    @Value
    class SendMessage {

        private final String topic;

        private final Long key;

        private final String message;
    }

    @Value
    public static class SubscribeToTopic {

        private final String topic;
    }
}
