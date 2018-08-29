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
## 点对点消息模式(队列queue)
### 发送消息
    发送消息时需要关闭连接对象，释放资源，连接对象关闭了，就没有必要再关闭Session, Producer
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
### 接收消息
        接收消息有两种方式，一种是使用`while (true)`, 另一种是使用监听器, 接收消息不需要关闭连接, 如果使用的是`while (true)`
    的方式接收消息, 则可以有关闭连接的代码，但没必要，而如果使用的是监听器的方式接收消息，则不可以关闭连接或者Session, 如果关闭
    了Session, 则一条消息也接收不到, 如果关闭了连接，则每次启动一次消费者的代码只能消费一条消息.
#### while (true)方式接收消息
```
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
```
#### 监听器方式接收消息
        使用监听器方式接收消息需要实现`MessageListener`接口，然后调用消费者的`setMessageListener`方法.
```$xslt
/**
 * 监听器的方式接收消息, 使用监听器的方式不能使用finally语句块关闭连接，否则
 * 每次启动一次main方法只能消费一条消息, Session也不能关闭，否则一条消息也接
 * 收不到.
 *
 * @author xzmeasy
 * @since 2018/8/29
 */
public class ActivemqListenerConsumer {

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
            messageConsumer.setMessageListener(message -> {
                if (Objects.nonNull(message) && message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        System.out.println("接收消息: " + textMessage.getText());
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
//            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
//        finally {
//            if (Objects.nonNull(connection)) {
//                try {
//                    connection.close();
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
```
## 发布(pub)/订阅(sub)消息模式(topic)
    需要先启动订阅者，才能启动发布者，否则订阅者将收不到消息.
### 发送消息
```$xslt
/**
 * 需要先启动订阅者，才能启动发布者，否则订阅者将收不到消息.
 *
 * @author xzmeasy
 * @since 2018/8/29
 */
public class ActivemqTopicProducer {

    /** 默认连接用户名 */
    private static final String USERNAME = ActiveMQConnection.DEFAULT_USER;

    /** 默认连接密码 */
    private static final String PASSWORD = ActiveMQConnection.DEFAULT_PASSWORD;

    /** 默认连接地址 */
    private static final String BROKERURL = ActiveMQConnection.DEFAULT_BROKER_URL;

    /** 设置发送消息的数量*/
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
            Destination destination = session.createTopic("MyTopic");
            // 创建消息生产者
            MessageProducer messageProducer = session.createProducer(destination);
            // 发送消息
            for (int i = 0; i < SEND_TIME; i++) {
                TextMessage textMessage = session.createTextMessage();
                textMessage.setText("ActiveMQ Message " + i);
                messageProducer.send(textMessage);
                System.out.println("发布消息: " + textMessage.getText());
            }
            // 提交当前事务中的所有的message, 并释放当前线程所持有的锁
            session.commit();
            // 关闭Session(Session关闭后, 没有必要关闭Producer和Consumer)
            // session.close;
        } catch (JMSException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,如果有关闭连接的代码，则不需要关闭Session, Producer, Consumer
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
### 接收消息
    创建两个消费者，需要先启动消费者，再启动发布者，否则消费者接收不到消息.
```$xslt
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
// 消费者2同理
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
