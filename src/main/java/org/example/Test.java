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
import org.apache.activemq.artemis.jms.client.ActiveMQConnection;

public class Test {

  public static void main(String[] args) {
//        System.setProperty("javax.net.ssl.trustStore","truststore.jks");

    Properties properties = new Properties();
    properties.put("java.naming.factory.initial", "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
    properties.put("connectionFactory.jms/TestJms/MainCF",
        "(tcp://jms-0.artemis.esb.df.cloud:443,tcp://jms-1.artemis.esb.df.cloud:443)?"
        + "sslEnabled=true"       //соединение по ssl
        + "&clientID=TestJMS"     //id клиента
        + "&ha=true" //Получение топологии сети кластера
        //Количество попыток подключения, если сервера недоступны. -1 - бесконечно. 0 - без попыток.
        //При указании 0 не будет работать переключение на следующий работающую ноды, если текущая лежит
        + "&reconnectAttempts=-1"
        //Максимально допустимое время задержки между попытками установления соединения
        + "&maxRetryInterval=5000"
        //Начальное время задержки между попытками установления соединения
        + "&retryInterval=1"
        //Множитель, увеличивающий время задержки при каждой попытке, но не больше maxRetryInterval
        //с retryInterval=1 и retryIntervalMultiplier=10.0 задержки будут 1ms, 10ms, 100ms, 1000ms, 10000ms
        + "&retryIntervalMultiplier=10.0"
        //Политика балансировки нагрузки - при каждом коннекте выбирается первый узел случайно,
        //А затем при реконнектах как Round Robin.
        + "&loadBalancingPolicyClassName=org.apache.activemq.artemis.api.core.client.loadbalance.RandomStickyConnectionLoadBalancingPolicy"
        //Отключаем проверку валидности сертификата брокера (только для тестов)
        + "&trustAll=true");
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
        Connection connection = connectionFactory.createConnection("admin", "admin!");
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer messageProducer = session.createProducer(queue);
    ) {
      for (int i = 0; i < 1000; i++) {
        String xml = "tru tu tu " + i;
        TextMessage textMessage = session.createTextMessage(xml);
        messageProducer.send(textMessage);
        System.out.println("sended to:"+getRemoteAddress(connection)+"; text:"+xml);
      }

    } catch (Exception e) {
      throw new RuntimeException("Can't send event because of " + e.getClass().getSimpleName() + ": " + e.getMessage(),
          e);
    }
  }

  private static String getRemoteAddress(Connection connection) {
    ActiveMQConnection amqConn = (ActiveMQConnection) connection;
    return amqConn.getSessionFactory().getConnection().getRemoteAddress();
  }
}
