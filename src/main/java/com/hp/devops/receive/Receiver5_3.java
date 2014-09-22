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
public class Receiver5_3 {

    private final static String QUEUE_NAME_SUFFIX = "_task_queue";

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;
    private static String DISTRIBUTOR_EXCHANGE;
    private static int MAX_NUM_OF_CONSUMERS;
    private static int MY_CONSUMER_INDEX;

    public static void main(String[] args) {

        Connection connection = null;
        Channel channel = null;

        if (args.length != 1) {
            System.out.println("You must provide unique consumer index between 0 and MAX_NUM_OF_CONSUMERS");
            System.exit(-1);
        }

        try {
            init(args[0]);

            if (MY_CONSUMER_INDEX < 0 || MY_CONSUMER_INDEX >= MAX_NUM_OF_CONSUMERS) {
                System.out.println("Invalid consumer index: you must provide unique consumer index between 0 and " + MAX_NUM_OF_CONSUMERS);
                System.exit(-1);
            }

            System.out.println("Starting consumer " + MY_CONSUMER_INDEX);

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(MQ_SERVER_NAME);
            factory.setPort(MQ_SERVER_PORT);
            factory.setUsername(MQ_SERVER_USERNAME);
            factory.setPassword(MQ_SERVER_PASSWORD);
            connection = factory.newConnection();
            channel = connection.createChannel();

            boolean isDurable = true;
            DISTRIBUTOR_EXCHANGE = QUEUE_NAME + "_TOPIC";
            channel.exchangeDeclare(DISTRIBUTOR_EXCHANGE, "topic", isDurable);

            channel.queueDeclare("Hello-" + MY_CONSUMER_INDEX, isDurable, false, false, null);
            channel.queueBind("Hello-" + MY_CONSUMER_INDEX, DISTRIBUTOR_EXCHANGE, "hello.#");
            channel.queueDeclare("Bye-" + MY_CONSUMER_INDEX, isDurable, false, false, null);
            channel.queueBind("Bye-" + MY_CONSUMER_INDEX, DISTRIBUTOR_EXCHANGE, "*.bye");

            QueueingConsumer consumer = new QueueingConsumer(channel);
            //ack will be sent explicitly
            boolean autoAck = false;
            channel.basicConsume("Hello-" + MY_CONSUMER_INDEX, autoAck, consumer);
            channel.basicConsume("Bye-" + MY_CONSUMER_INDEX, autoAck, consumer);

            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                String message = new String(delivery.getBody());
                String routingKey = delivery.getEnvelope().getRoutingKey();
                System.out.println(" [x] Received '" + message + "' with routing key " + routingKey);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException ignore) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException ignore) {
                }
            }
        }

    }

    private static void init(String myNumber) {
        Properties properties = PropertiesLoader.getProperties("app");
        MQ_SERVER_NAME = properties.getProperty("MQ_SERVER_NAME");
        MQ_SERVER_PORT = Integer.parseInt(properties.getProperty("MQ_SERVER_PORT"));
        MQ_SERVER_USERNAME = properties.getProperty("MQ_SERVER_USERNAME");
        MQ_SERVER_PASSWORD = properties.getProperty("MQ_SERVER_PASSWORD");
        QUEUE_NAME = properties.getProperty("QUEUE_NAME");
        MAX_NUM_OF_CONSUMERS = Integer.parseInt(properties.getProperty("NUM_OF_CONSUMERS"));
        MY_CONSUMER_INDEX = Integer.parseInt(myNumber);
    }


}
