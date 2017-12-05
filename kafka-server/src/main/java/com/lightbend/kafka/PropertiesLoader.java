package com.lightbend.kafka;

import java.io.*;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties from(String file) {
        Properties properties = new Properties();

        InputStream is = Class.class.getResourceAsStream("/kafkalocal.properties");

        if (is == null) {
            File f = new File(file);

            if (f.isFile()) {
                try {
                    is = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(String.format("file %s not found as classpath resources or on the filesystem"));
                }
            } else {
                throw new IllegalArgumentException(String.format("file %s not found as classpath resources or on the filesystem"));
            }
        }

        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }
}
