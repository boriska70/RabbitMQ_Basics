package com.hp.devops.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: belozovs
 * Date: 9/21/14
 * Description
 */
public class PropertiesLoader {

    private static AtomicInteger publisherCounter = new AtomicInteger(0);
    private static AtomicInteger consumerCounter = new AtomicInteger(0);

    public static Properties getProperties(String fileName) {
        Properties properties = new Properties();
        InputStream is = PropertiesLoader.class.getClassLoader().getResourceAsStream("./META-INF/" + fileName + ".properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            System.out.println("Cannot load properties file, exiting...");
            System.exit(-1);
        }
        return properties;
    }

    public static int getNextPublisherIndex(){
        return publisherCounter.getAndIncrement();
    }

}
