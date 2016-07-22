package com.youzan.sz.common.nsq;

import com.alibaba.fastjson.JSON;
import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jinxiaofei on 16/7/20.
 */
public class NSQclient {
    private static final Logger LOGGER = LoggerFactory.getLogger(NSQclient.class);
    /**
     * 默认的线程池大小
     */
    private static final int DEFAULT_POOLSIZE4IO = 1;
    /**
     * 默认的完成时间
     * Perform one action during specified timeout
     */
    private static final int DEFAULT_TIMEOUT_SECONDS = 1;
    /**
     * 默认的消息发送超时时间
     */
    private static final int DEFAULT_MSG_TIMEOUT_MILLSECONDS = 60 * 1000;


    /**
     * 根据配置来注册消息
     *
     * @param config
     * @param
     */
    public static <T extends NSQmsgBean> void regester(NSQConfig config, T message) {
        final Producer producer = new ProducerImplV2(config);
        try {
            producer.start();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("开始注册消息队列的信息,topic:{},data:{}", config.getTopic(), message);
            }
            producer.publish(JSON.toJSONBytes(message));
        } catch (NSQException e) {
            e.printStackTrace();
        } finally {
            // 一定要在finally或者进程退出的时候,里做下优雅的关闭,通常是进程退出前
            //jvm 关闭时 优雅关闭nsq
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {

                    // 一定要在finally里做下优雅的关闭,通常是进程退出前
                    LOGGER.info("close nsq consumer");
                    if (producer != null) {
                        producer.close();
                    }


                }
            });
        }
    }


    /**
     * @param topic
     * @param data
     */
    public static <T extends NSQmsgBean> void regester(String topic, String lookup, T data) {
        //String lookup=PropertiesUtils.getProperty("","");
        if (lookup == null || lookup.isEmpty()) {
            LOGGER.error("can't read NSQ address,please check the properties file path is success?");
            return;
        }
        try {
            final NSQConfig config = new NSQConfig();
            config.setLookupAddresses(lookup);
            config.setThreadPoolSize4IO(DEFAULT_POOLSIZE4IO);
            config.setTimeoutInSecond(DEFAULT_TIMEOUT_SECONDS);
            config.setMsgTimeoutInMillisecond(DEFAULT_MSG_TIMEOUT_MILLSECONDS);
            config.setTopic(topic);
            regester(config, data);
        } catch (NSQException e) {
            LOGGER.error("发送消息队列,出现错误,topic:{},data:{},Exception:{}", topic, data, e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param topic    主题
     * @param lookup
     * @param callback
     * @param clazz
     */
    public static void listener(String topic, String lookup, ConsumerCallback callback, String consumerName, Class<? extends NSQmsgBean> clazz) {
        //String lookup=PropertiesUtils.getProperty("","");
        if (lookup == null || lookup.isEmpty()) {
            LOGGER.error("can't read NSQ address,please check the properties file path is success?");
            return;
        }
        if (consumerName == null || consumerName.isEmpty()) {
            LOGGER.error("应用名称不能为空,请检查");
            return;
        }
        try {
            final NSQConfig config = new NSQConfig();
            config.setLookupAddresses(lookup);
            config.setThreadPoolSize4IO(DEFAULT_POOLSIZE4IO);
            config.setTimeoutInSecond(DEFAULT_TIMEOUT_SECONDS);
            config.setMsgTimeoutInMillisecond(DEFAULT_MSG_TIMEOUT_MILLSECONDS);
            config.setConsumerName(consumerName);
            config.setTopic(topic);

        } catch (NSQException e) {
            LOGGER.error("发送消息队列,出现错误,topic:{},data:{},Exception:{}", topic, e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     * @param config
     * @param callback
     * @param clazz,反序列化的类
     */

    public static void listener(NSQConfig config, ConsumerCallback callback, Class<? extends NSQmsgBean> clazz) {
        /**
         * 消费需要放在自己的线程处理;
         */
        new Thread(()->{
            try {
                final Consumer consumer = new ConsumerImplV2(config, (message) -> {
                    LOGGER.info("消费者:{}获取了消息:{},内容:{}", config.getConsumerName(), message.getReadableContent());
                    String msg=message.getReadableContent();
                    callback.callback(JSON.parseObject(msg,clazz));
                    // 设置了不合法(经过多久后)下次消费
                    try {
                        message.setNextConsumingInSecond(null);
                    } catch (NSQException e) {
                        e.printStackTrace();
                    }
                });
                consumer.start();
                // 一定要在finally或者进程退出的时候,里做下优雅的关闭,通常是进程退出前
                //jvm 关闭时 优雅关闭nsq
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {

                        // 一定要在finally里做下优雅的关闭,通常是进程退出前
                        LOGGER.info("close nsq consumer");
                        if (consumer != null) {
                            consumer.close();
                        }


                    }
                });


            } catch (NSQException e) {
                LOGGER.error("发送消息队列,出现错误,topic:{},Exception:{}", config.getTopic(), e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


    }
}