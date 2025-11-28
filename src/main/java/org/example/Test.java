package org.example;

import java.util.Properties;

import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Test {
    public static void main(String[] args) {
//        System.setProperty("javax.net.ssl.trustStore","truststore.jks");

        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        properties.put("connectionFactory.jms/TestJms/MainCF",
                "tcp://jms-0.artemis.esb.df.cloud:443?type=QUEUE_CF&sslEnabled=true&clientID=TestJMS&trustAll=true");
        properties.put("queue.jms/TestJms/in", "client.fido_adapter.in");

        QueueConnectionFactory queueConnectionFactory = null;
        Queue queue = null;

        try {

            Context initialContext = new InitialContext(properties);
            queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup("jms/TestJms/MainCF");
            queue = (Queue) initialContext.lookup("jms/TestJms/in");
        } catch (Exception e) {
            throw new RuntimeException("Can't lookup administrative objects because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        try (
                QueueConnection queueConnection = queueConnectionFactory.createQueueConnection("admin", "admin!");
                Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer messageProducer = session.createProducer(queue);
        ) {
            String xml = "tru tu tu";
            TextMessage textMessage = session.createTextMessage(xml);
            messageProducer.send(textMessage);
        } catch (Exception e) {
            throw new RuntimeException("Can't send event because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
