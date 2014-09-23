package com.hp.devops.send;

import com.hp.devops.utils.PropertiesLoader;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * User: belozovs
 * Date: 9/21/14
 * Description
 */
public class Sender5_1 {

    private static String MQ_SERVER_NAME;
    private static int MQ_SERVER_PORT;
    private static String MQ_SERVER_USERNAME;
    private static String MQ_SERVER_PASSWORD;
    private static String QUEUE_NAME;
    private static String GATEWAY_EXCHANGE, DISTRIBUTOR_EXCHANGE;

    private static volatile SortedSet<Long> unconfirmedSet = Collections.synchronizedSortedSet(new TreeSet<Long>());

    public static void main(String[] args) throws IOException, InterruptedException {

        init();

        String message = args.length == 0 ? "Hello World." : args[0];
        String routingKey = args[1];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(MQ_SERVER_NAME);
        factory.setPort(MQ_SERVER_PORT);
        factory.setUsername(MQ_SERVER_USERNAME);
        factory.setPassword(MQ_SERVER_PASSWORD);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        GATEWAY_EXCHANGE = QUEUE_NAME + "_FANOUT";
        DISTRIBUTOR_EXCHANGE = QUEUE_NAME + "_TOPIC";

        boolean isDurable = true;
        //Channel declares the DURABLE TOPIC exchange to allow flexible multiple bindings
        channel.exchangeDeclare(GATEWAY_EXCHANGE, "fanout", isDurable);
        channel.exchangeDeclare(DISTRIBUTOR_EXCHANGE, "topic", isDurable);
        channel.exchangeBind(DISTRIBUTOR_EXCHANGE, GATEWAY_EXCHANGE, "#");

        //Very guaranteed delivery but very heavy - see readme, that's why it is commented out
        //channel.txSelect();

        //Publisher Confirm - guaranteed delivery x100 times faster than the transactional one

        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long seqNo, boolean multiple) throws IOException {
                if (multiple) {
                    unconfirmedSet.headSet(seqNo + 1).clear();
                }
                else {
                    unconfirmedSet.remove(seqNo);
                }
                System.out.println(" [x] Sent a message with sequence number " + seqNo);
            }

            @Override
            public void handleNack(long seqNo, boolean multiple) throws IOException {
                System.out.println("No ack for sequence number " + seqNo);
            }
        });
        channel.confirmSelect();

        //Messages published with the routing key
        //Messages will be lost, if no queue is bound to exchange yet, consumers won't receive old messages
        unconfirmedSet.add(channel.getNextPublishSeqNo());
        for (int i = 0; i < 10; i++) {
            long nextSeqNo = channel.getNextPublishSeqNo();
            channel.basicPublish(GATEWAY_EXCHANGE, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, (message+" "+i).getBytes());
            System.out.println(" Sending '" + (message+" "+i) + "' with routing key " + routingKey + ". Waiting for ack for message number " + nextSeqNo);
        }
        try {
            channel.waitForConfirmsOrDie(10000);
        } catch (TimeoutException e) {
            System.out.println("There are " + unconfirmedSet.size() + " unconfirmed messages even after 10 seconds of waiting. Too bad...");
        }

        //Very guaranteed delivery but very heavy - see readme, that's why it is commented out
        //channel.txCommit();




        channel.close();
        connection.close();

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
