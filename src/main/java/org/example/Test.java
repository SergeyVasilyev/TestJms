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
        System.setProperty("javax.net.ssl.trustStore","truststore.jks");

        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
        properties.put("connectionFactory.jms/flexcube/OutboundEventArtemisQueueConnectionFactory",
                "tcp://uz-is.dafr.loc:61616?type=QUEUE_CF&sslEnabled=true&clientID=FlexCube");
        properties.put("queue.jms/flexcube/OutboundEventArtemisQueue", "FcOutboundEventQueue");

        QueueConnectionFactory queueConnectionFactory = null;
        Queue queue = null;

        try {

            Context initialContext = new InitialContext(properties);
            queueConnectionFactory = (QueueConnectionFactory) initialContext.lookup("jms/flexcube/OutboundEventArtemisQueueConnectionFactory");
            queue = (Queue) initialContext.lookup("jms/flexcube/OutboundEventArtemisQueue");
        } catch (Exception e) {
            throw new RuntimeException("Can't lookup administrative objects because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        try (
                QueueConnection queueConnection = queueConnectionFactory.createQueueConnection("flexcube", "flexcube");
                Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageProducer messageProducer = session.createProducer(queue);
        ) {
            String xml = "...";
            TextMessage textMessage = session.createTextMessage(xml);
            messageProducer.send(textMessage);
        } catch (Exception e) {
            throw new RuntimeException("Can't send event because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
