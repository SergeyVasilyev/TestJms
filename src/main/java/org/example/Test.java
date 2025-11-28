package org.example;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.MessageProducer;
import jakarta.jms.Queue;
import jakarta.jms.QueueConnectionFactory;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Test {

  public static void main(String[] args) {
//        System.setProperty("javax.net.ssl.trustStore","truststore.jks");

    Properties properties = new Properties();
    properties.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
    properties.put("connectionFactory.jms/TestJms/MainCF",
        "tcp://jms-0.artemis.esb.df.cloud:443?&sslEnabled=true&clientID=TestJMS&trustAll=true");
    properties.put("queue.jms/TestJms/in", "client.fido_adapter.in");

    ConnectionFactory connectionFactory = null;
    Queue queue = null;

    try {

      Context initialContext = new InitialContext(properties);
      connectionFactory = (QueueConnectionFactory) initialContext.lookup("jms/TestJms/MainCF");
      queue = (Queue) initialContext.lookup("jms/TestJms/in");
    } catch (Exception e) {
      throw new RuntimeException(
          "Can't lookup administrative objects because of " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    }

    try (
        Connection queueConnection = connectionFactory.createConnection("admin", "admin!");
        Session session = queueConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer messageProducer = session.createProducer(queue);
    ) {
      for (int i = 0; i < 1000; i++) {
        String xml = "tru tu tu " + i;
        TextMessage textMessage = session.createTextMessage(xml);
        messageProducer.send(textMessage);
      }

    } catch (Exception e) {
      throw new RuntimeException("Can't send event because of " + e.getClass().getSimpleName() + ": " + e.getMessage(),
          e);
    }
  }
}
