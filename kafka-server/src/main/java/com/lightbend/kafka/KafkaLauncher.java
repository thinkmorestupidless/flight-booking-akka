package com.lightbend.kafka;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class KafkaLauncher {

    static File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    static int KAFKA_PORT = 9092;
    static String KAFKA_DATA_DIR = new File(TEMP_DIR,"kafka_data").getAbsolutePath();

    static int ZK_PORT = 2181;
    static File ZK_DATA_DIR = new File(TEMP_DIR, "zookeeper_data");

    private static ZookeeperLocalServer zookeeper;

    private static KafkaLocalServer kafka;

    public static void main(String[] args) throws Exception {

        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }

        //load properties
        Properties kafkaProperties = PropertiesLoader.from("/kafkalocal.properties");
        Properties zkProperties = PropertiesLoader.from("/zklocal.properties");

        kafkaProperties.setProperty("log.dirs", KAFKA_DATA_DIR);
        kafkaProperties.setProperty("listeners", String.format("PLAINTEXT://:%s", KAFKA_PORT));
        kafkaProperties.setProperty("zookeeper.connect", String.format("localhost:%s", ZK_PORT));

        //start zookeeper
        zookeeper = new ZookeeperLocalServer(zkProperties, ZK_PORT, ZK_DATA_DIR);

        //start kafka
        kafka = new KafkaLocalServer(kafkaProperties);
    }
}
