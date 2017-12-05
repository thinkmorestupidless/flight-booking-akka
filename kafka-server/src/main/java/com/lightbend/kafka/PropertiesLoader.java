package com.lightbend.kafka;

import java.io.*;
import java.util.Properties;

public class PropertiesLoader {

    public static Properties from(String file) {

        Properties properties = new Properties();

        InputStream is = PropertiesLoader.class.getResourceAsStream(file);

        if (is == null) {
            File f = new File(file);

            if (f.isFile()) {
                try {
                    is = new FileInputStream(f);
                } catch (FileNotFoundException e) {
                    throw new IllegalArgumentException(String.format("file %s not found as classpath resources or on the filesystem"));
                }
            } else {
                throw new IllegalArgumentException(String.format("file %s not found as classpath resources or on the filesystem", file));
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
