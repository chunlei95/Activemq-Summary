package com.java.activemq.queue.consumer;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

/**
 * 消费者消费消息不需要有关闭连接或关闭Session的代码.
 * 使用While (true) 的方式即使有关闭连接或者关闭Session的代码也没影响，但是如果
 * 使用的是监听器的方式的话则会导致结果不是预期的.
 *
 * @author xzmeasy
 * @since 2018/8/29
 */
public class ActivemqConsumer {

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
            Destination destination = session.createQueue("MyQueue");
            MessageConsumer messageConsumer = session.createConsumer(destination);
            // 还有一种接受消息的方式是使用监听器, 实现MessageListener接口
            while (true) {
                // 接收消息
                Message message = messageConsumer.receive(100000);
                if (Objects.nonNull(message) && message instanceof TextMessage) {
                    // 转化为TextMessage类型，因为Producer例子中发送的消息是TextMessage类型
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println("接受到的消息: " + textMessage.getText());
                } else {
                    break;
                }
            }
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
