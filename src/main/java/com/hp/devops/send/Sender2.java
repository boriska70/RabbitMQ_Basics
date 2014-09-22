package com.hp.devops.send;

import com.hp.devops.utils.PropertiesLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.Properties;

/**
 * User: belozovs
 * Date: 9/21/14
 * Description
 */
public class Sender2 {

    private final static String QUEUE_NAME_SUFFIX = "_task_queue";

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;

    public static void main(String[] args) throws IOException {

        init();

        String message = args.length == 0 ? "Hello World." : args[0];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_SERVER_NAME);
        factory.setPort(MQ_SERVER_PORT);
        factory.setUsername(MQ_SERVER_USERNAME);
        factory.setPassword(MQ_SERVER_PASSWORD);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //Define the queue as durable - every persisted message will be saved on disk for the case, when RabbitMQ crashes
        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME+QUEUE_NAME_SUFFIX, durable, false, false, null);

        //Tell the server that the message is persisted, must be sent to durable queue
        channel.basicPublish("", QUEUE_NAME+QUEUE_NAME_SUFFIX, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        System.out.println(" [x] Sent'" + message +"'");

        channel.close();
        connection.close();

    }

    private static void init(){
        Properties properties = PropertiesLoader.getProperties("app");
        MQ_SERVER_NAME = properties.getProperty("MQ_SERVER_NAME");
        MQ_SERVER_PORT = Integer.parseInt(properties.getProperty("MQ_SERVER_PORT"));
        MQ_SERVER_USERNAME = properties.getProperty("MQ_SERVER_USERNAME");
        MQ_SERVER_PASSWORD = properties.getProperty("MQ_SERVER_PASSWORD");
        QUEUE_NAME = properties.getProperty("QUEUE_NAME");
    }

}
