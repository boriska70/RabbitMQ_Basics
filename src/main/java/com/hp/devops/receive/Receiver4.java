package com.hp.devops.receive;

import com.hp.devops.utils.PropertiesLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.Properties;

/**
 * User: belozovs
 * Date: 9/21/14
 * Description
 */
public class Receiver4 {

    private final static String QUEUE_NAME_SUFFIX = "_task_queue";

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;
    private static String EXCHANGE_NAME;

    public static void main(String[] args) throws IOException, InterruptedException {

        init();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_SERVER_NAME);
        factory.setPort(MQ_SERVER_PORT);
        factory.setUsername(MQ_SERVER_USERNAME);
        factory.setPassword(MQ_SERVER_PASSWORD);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //declare exchange
        EXCHANGE_NAME = QUEUE_NAME+"_EXCHANGE_FOR_ROUTING";
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        //dynamically get the real queue name generated by the server...
        String actualQueueName = channel.queueDeclare().getQueue();
        //... and bind it TWICE to the exchange on the receiver side - once for hello routing key and once for bye routing key
        channel.queueBind(actualQueueName, EXCHANGE_NAME, "hello");
        channel.queueBind(actualQueueName, EXCHANGE_NAME, "bye");
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(actualQueueName, true, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            String routingKey = delivery.getEnvelope().getRoutingKey();
            System.out.println(" [x] Received '" + message + "' with routing key " + routingKey);
        }

    }

    private static void init() {
        Properties properties = PropertiesLoader.getProperties("app");
        MQ_SERVER_NAME = properties.getProperty("MQ_SERVER_NAME");
        MQ_SERVER_PORT = Integer.parseInt(properties.getProperty("MQ_SERVER_PORT"));
        MQ_SERVER_USERNAME = properties.getProperty("MQ_SERVER_USERNAME");
        MQ_SERVER_PASSWORD = properties.getProperty("MQ_SERVER_PASSWORD");
        QUEUE_NAME = properties.getProperty("QUEUE_NAME");
    }


}
