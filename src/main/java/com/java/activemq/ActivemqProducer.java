package com.java.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Objects;

/**
 * @author xzmeasy
 * @since 18-8-26
 */
@SuppressWarnings("SpellCheckingInspection")
public class ActivemqProducer {

    // 默认连接用户名
    private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;

    // 默认连接密码
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;

    // 默认连接地址
    private static final String BROKERURL = ActiveMQConnection.DEFAULT_BROKER_URL;

    // 设置发送消息的数量
    private static final Integer SEND_TIME = 10;

    public static void main(String[] args) {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKERURL);
        Connection connection = null;
        try {
            // 创建连接对象
            connection = connectionFactory.createConnection();
            // 创建会话(Session)对象, 第一个参数表示是否添加事务(通常设置为true), 第二个参数表示确认方式
            Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            // 创建消息发送目的地
            Destination destination = session.createQueue("MyQueue");
            // 创建消息生产者
            MessageProducer messageProducer = session.createProducer(destination);
            // 发送消息
            for (int i = 0; i < SEND_TIME; i++) {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText("ActiveMQ Message " + i);
                messageProducer.send(textMessage);
                System.out.println("发送消息: " + textMessage.getText());
            }
            // 提交当前事务中的所有的message, 并释放当前线程所持有的锁
            session.commit();
            // 关闭Session(Session关闭后, 没有必要关闭Producer和Consumer)
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (Objects.nonNull(connection)) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
