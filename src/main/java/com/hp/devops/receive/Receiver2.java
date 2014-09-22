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
public class Receiver2 {

    private final static String QUEUE_NAME_SUFFIX = "_task_queue";

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;

    public static void main(String[] args) throws IOException, InterruptedException {

        init();

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
        //Fair dispatching - consumer won't get another message if it didn't "acked" the previous one
        channel.basicQos(1);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        QueueingConsumer consumer = new QueueingConsumer(channel);
        //do not immediately delete message from queue, wait for the processing finish
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME+QUEUE_NAME_SUFFIX, autoAck, consumer);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            String message = new String(delivery.getBody());
            System.out.println(" [x] Received '" + message + "'");
            doWork(message);
            System.out.println(" [x] Done");
            //explicitly tell the server to remove the message as it was received and processed
            //otherwise server memory leak will take place
            //use this command to verify you have no such leaked messages: rabbitmqctl list_queues name messages_ready messages_unacknowledged
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }

    }

    private static void doWork(String message) throws InterruptedException {

        for (char ch : message.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000);
            }
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
