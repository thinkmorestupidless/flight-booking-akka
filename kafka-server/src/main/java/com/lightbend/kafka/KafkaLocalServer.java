package com.lightbend.kafka;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class KafkaLocalServer {

    private static final Logger log = LoggerFactory.getLogger(KafkaLocalServer.class);

    public KafkaServerStartable kafka;

    public KafkaLocalServer(Properties kafkaProperties) throws Exception {
        log.info("starting local kafka server");

        KafkaConfig kafkaConfig = new KafkaConfig(kafkaProperties);

        kafka = new KafkaServerStartable(kafkaConfig);
        kafka.startup();
    }


    public void stop(){
        //stop kafka broker
        kafka.shutdown();
    }
}
