package com.youzan.sz.common.nsq;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * Created by jinxiaofei on 16/7/20.
 * 为了方便双方使用,消息体类(继承与NsqMessage)请写在jar包中
 * <p>
 * 本客户端依赖配置明细 : nsq.host:
 */
public class NSQclient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSQclient.class);
    /**
     * 默认的线程池大小
     */
    private static final int DEFAULT_POOLSIZE4IO = 2;
    /**
     * 默认的完成时间
     * Perform one action during specified timeout
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 3;
    /**
     * 默认的消息发送超时时间
     */
    private static final int DEFAULT_MSG_TIMEOUT_MILLSECONDS = (int) TimeUnit.MINUTES.toMillis(1);

    /**
     * 集群(多)地址
     */
    private static final String LOOKUP = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host", "nsq-qa.s.qima-inc.com:4161");

    /**
     * 发布消息
     *
     * @param topic
     * @param messageBean
     * @param <T>
     */
    public static <T extends NsqMessage> void releaseMessage(String topic, T messageBean) {
        publish(topic, messageBean.encode());
    }

    private static void publish(String topic, byte[] message) {
        // 创建配置: 要连接的集群参数和本机进程参数
        final NSQConfig config = newNSQConfig();
        // 设置Topic Name
        config.setTopic(topic);
        // 设置Lookupd集群(多)地址, 是以","分隔的字符串,就是说可以配置一个集群里的多个节点
        config.setLookupAddresses(LOOKUP);
        // 设置Netty里的ThreadPoolSize(带默认值): 1Thread-to-1IOThread, 使用BlockingIO
        config.setThreadPoolSize4IO(DEFAULT_POOLSIZE4IO);
        // 设置timeout(带默认值): 一次IO来回+本机执行了返回给client code完成的消耗时间
        config.setTimeoutInSecond(DEFAULT_TIMEOUT_SECONDS);
        // 设置message中client-server之间可以的timeout(带默认值)
        config.setMsgTimeoutInMillisecond(DEFAULT_MSG_TIMEOUT_MILLSECONDS);
        try (Producer p = new ProducerImplV2(config)) {
            p.start();
            p.publish(message);
            LOGGER.info("NSQ推送成功 topic:{}, message:{}",topic,new String(message, "UTF-8"));
        } catch (NSQException e) {
            LOGGER.warn("NSQ推送失败 e:{}", e.getMessage());
            throw new BusinessException((long) ResponseCode.NSQ_EXCEPTION.getCode(), e.getMessage());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册一个消费者
     *
     * @param topic        话题
     * @param consumerName 消费者名称
     * @param callback     回调函数
     * @param clazz        消息体类型
     */
    public static void registersTheConsumer(String topic, String consumerName, ConsumerCallback callback, Class<? extends NsqMessage> clazz) {
        new Thread(() -> {
            consume(topic, consumerName, callback, clazz);
        }).start();
    }

    private static void consume(String topic, String consumerName, ConsumerCallback callback, Class<? extends NsqMessage> clazz) {
        final NSQConfig config = newNSQConfig();
        config.setLookupAddresses(LOOKUP);
        config.setThreadPoolSize4IO(1);
        config.setTimeoutInSecond(120);
        config.setMsgTimeoutInMillisecond(120 * 1000);
        config.setTopic(topic);
        config.setConsumerName(consumerName);
        LOGGER.info("开始注册消费者 topic:{} consumerName:{}", topic, consumerName);
        try (final Consumer consumer = newConsumer(config, callback, clazz)) {
            consumer.start();
        } catch (NSQException e) {
            throw new BusinessException((long) ResponseCode.NSQ_EXCEPTION.getCode(), e.getMessage());
        }
    }

    private static NSQConfig newNSQConfig() {
        try {
            return new NSQConfig();
        } catch (NSQException e) {
            throw new BusinessException((long) ResponseCode.NSQ_EXCEPTION.getCode(), e.getMessage());
        }
    }

    private static Consumer newConsumer(NSQConfig config, ConsumerCallback callback, Class<? extends NsqMessage> clazz) {
        return new ConsumerImplV2(config, (message) -> {
//            Assert.assertNotNull(message);
            callback.callback(decode(message.getReadableContent(), clazz));
            LOGGER.info("消费者获取了消息:{},内容:{}", message, message.getReadableContent());
            try {
                message.setNextConsumingInSecond(null);
            } catch (NSQException e) {
                e.printStackTrace();
            }
        });
    }

    private static <T extends NsqMessage> T decode(String message, Class<T> clazz) {
        try {
            return clazz.newInstance().decode(message);
        } catch (InstantiationException e) {
            LOGGER.warn("NSQ 信息解码失败,请查找问题 message:{} error:{}", message, e.getMessage());
            throw new BusinessException((long) ResponseCode.NSQ_EXCEPTION.getCode(), e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.warn("NSQ 信息解码失败,请查找问题 error:{}", e.getMessage());
            throw new BusinessException((long) ResponseCode.NSQ_EXCEPTION.getCode(), e.getMessage());
        }
    }

    public static void main(String[] args) {
        NSQclient.registersTheConsumer("test_jinxiaofei","demoConsumer1",new DemoConsumerCallback("demoConsumer1"), DemoNsqMessage.class);
        NSQclient.registersTheConsumer("test_jinxiaofei","demoConsumer2",new DemoConsumerCallback("demoConsumer2"), DemoNsqMessage.class);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NSQclient.releaseMessage("test_jinxiaofei",new DemoNsqMessage("金小飞","基德是个老司机"));
    }


}

class DemoConsumerCallback implements ConsumerCallback {
    private String consumerName;

    public DemoConsumerCallback(String consumerName) {
        this.consumerName = consumerName;
    }

    @Override
    public <T extends NsqMessage> void callback(T message) {
        System.out.println("我是 "+consumerName+" 我收到了信息 "+message.toString());
    }
}

class DemoNsqMessage extends NsqMessage{

    private String name;

    private String message;

    public DemoNsqMessage() {
    }

    public DemoNsqMessage(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public byte[] encode() {
        return JsonUtils.bean2Json(this).getBytes();
    }

    @Override
    public <T extends NsqMessage> T decode(String message) {
        return JsonUtils.json2Bean(message, (Class<T>) DemoNsqMessage.class);
    }

    @Override
    public String toString(){
        return name + "说:" + message;
    }
}