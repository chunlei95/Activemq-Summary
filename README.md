# Activemq-Summary
activemq学习总结
## ActiveMQ介绍
    ActiveMQ 是Apache出品的开源消息总线
## 对象模型
* ActiveMQConnectionFactory(连接工厂)
* ActiveMQConnection(连接对象)
* ActiveMQSession(会话对象)
* ActiveMQDestination(消息发送目的地)
* ActiveMQMessageProducer(消息生产者)
* ActiveMQMessageConsumer(消息消费者)
* ActiveMQMessage(消息对象)
## 发送消息
```
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
```
## 接收消息
```

```
## 对象模型详解
### ActiveMQConnectionFactory
        ActiveMQConnectionFactory是用来创建连接(Connection)的对象, 它实现了QueueConnectionFactory以及
    TopicConnectionFactory, 所以可以用它来创建QueueConnection以及TopicConnection。
#### 构造函数
##### ActiveMQConnectionFactory()
    默认构造函数, 使用该构造函数创建ActiveMQConnectionFactory对象会使用默认的连接用户名, 连接密码以及连接地址, 
    默认的连接地址为`ActiveMQConnectionFactory.DEFAULT_USER`, 
    默认的连接密码为`ActiveMQConnectionFactory.DEFAULT_PASSWORD`,
    默认的连接地址为`ActiveMQConnectionFactory.DEFAULT_BROKER_URL`, 
    默认连接地址中的主机号为`localhost`, 端口号为`61616`.
##### ActiveMQConnectionFactory(String brokerURL)
##### ActiveMQConnectionFactory(URI brokerURL)
##### ActiveMQConnectionFactory(String userName, String password, URI brokerURL)
##### ActiveMQConnectionFactory(String userName, String password, String brokerURL)
#### 方法
##### createConnection()
##### createConnection(String userName, String password)
##### QueueConnection createQueueConnection()
##### createQueueConnection(String userName, String password)
##### createTopicConnection()
##### createTopicConnection(String userName, String password)
##### createActiveMQConnection()
##### createActiveMQConnection(String userName, String password)
##### createActiveMQConnection(Transport transport, JMSStatsImpl stats)
### ActiveMQConnection

### ActiveMQSession

### ActiveMQDestination

### ActiveMQMessageProducer

### ActiveMQMessageConsumer

### ActiveMQMessage
