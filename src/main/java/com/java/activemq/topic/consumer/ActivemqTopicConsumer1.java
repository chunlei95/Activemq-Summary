package com.java.activemq.topic.consumer;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

/**
 * @author xzmeasy
 * @since 2018/8/29
 */
public class ActivemqTopicConsumer1 {

    /**
     * 默认连接用户名
     */
    private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;

    /**
     * 默认连接密码
     */
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;

    /**
     * 默认连接地址
     */
    private static final String BROKER_URL = ActiveMQConnection.DEFAULT_BROKER_URL;

    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);
        Connection connection = null;
        try {
            // 创建连接对象
            connection = connectionFactory.createConnection();
            // 开启连接
            connection.start();
            // 消费者消费消息的时候不需要在事务中
            Session session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
            // 消息目的地的名称需要和生产者的保持一致，才能够消费到名称对应的生产这生产的消息
            Destination destination = session.createTopic("MyTopic");
            MessageConsumer messageConsumer = session.createConsumer(destination);
            // 还有一种接受消息的方式是使用监听器, 实现MessageListener接口
            messageConsumer.setMessageListener(message -> {
                if (Objects.nonNull(message) && message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("订阅者 1 接收消息: " + textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
