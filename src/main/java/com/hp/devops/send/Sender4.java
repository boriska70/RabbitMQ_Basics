package com.hp.devops.send;

import com.hp.devops.utils.PropertiesLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * User: belozovs
 * Date: 9/21/14
 * Description
 */
public class Sender4 {

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;
    private static String EXCHANGE_NAME;

    public static void main(String[] args) throws IOException {

        init();

        String message = args.length == 0 ? "Hello World." : args[0];
        String routingKey = "hello";
        if(args.length == 2 && args[1].equals("bye")){
            routingKey = "bye";
        }

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_SERVER_NAME);
        factory.setPort(MQ_SERVER_PORT);
        factory.setUsername(MQ_SERVER_USERNAME);
        factory.setPassword(MQ_SERVER_PASSWORD);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        EXCHANGE_NAME = QUEUE_NAME+"_EXCHANGE_FOR_ROUTING";

        //Channel declares the DIRECT exchange to switch to multicast rather than to broadcast as in case of FANOUT exchange
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        //Messages published with the routing key
        //Messages will be lost, if no queue is bound to exchange yet, consumers won't receive old messages
        channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
        System.out.println(" [x] Sent'" + message + "' with routing key "+routingKey);

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
