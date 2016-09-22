package com.youzan.sz.nsq;

import com.youzan.nsq.client.entity.NSQConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQClientInitializer<T extends NSQMsg> implements ClientInitializer {
    protected final Logger logger   = LoggerFactory.getLogger(getClass());
    protected NSQConfig    nsqConfig;
    protected NSQCodec     nsqCodec = null;
    protected String       topic    = null;
    protected String       consumerName;
    protected Integer      connectTimeoutInMillisecond;
    protected Integer      msgTimeoutInMillisecond;

    protected AbstractNSQClientInitializer() {
        try {
            nsqConfig = new NSQConfig();
        } catch (RuntimeException e) {
            logger.error("init exception", e);
        }
    }

    public AbstractNSQClientInitializer setLookUp(String... lookUps) {
        if (lookUps != null && lookUps.length > 0) {
            StringBuilder lp = new StringBuilder();
            for (String lookUp : lookUps) {
                lp.append(lookUp).append(",");
            }
            nsqConfig.setLookupAddresses(lp.toString());
        }
        nsqConfig.setThreadPoolSize4IO(1);
        nsqConfig.setConnectTimeoutInMillisecond(
            getConnectTimeoutInMillisecond() == null ? 3 * 1000 : getConnectTimeoutInMillisecond());
        nsqConfig.setMsgTimeoutInMillisecond(
            getMsgTimeoutInMillisecond() == null ? (int) TimeUnit.SECONDS.toMillis(120) : getMsgTimeoutInMillisecond());
        return this;
    }

    /**
     * 设置主题
     * */
    public AbstractNSQClientInitializer setTopic(String topic) {
        if (StringUtils.isEmpty(topic)) {
            throw new NullPointerException("nsq topic is not null");
        }
        this.topic = topic;
        return this;
    }

    public String getTopic() {
        return this.topic;
    }

    public AbstractNSQClientInitializer setCodec(NSQCodec nsqCodec) {
        this.nsqCodec = nsqCodec;
        return this;
    }

    public AbstractNSQClientInitializer setConsumerName(String consumerName) {
        this.consumerName = consumerName;
        return this;
    }

    public NSQCodec getNsqCodec() {
        return nsqCodec;
    }

    public NSQConfig getNsqConfig() {
        return nsqConfig;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public Integer getConnectTimeoutInMillisecond() {
        return connectTimeoutInMillisecond;
    }

    public AbstractNSQClientInitializer<T> setConnectTimeoutInMillisecond(Integer connectTimeoutInMillisecond) {
        this.connectTimeoutInMillisecond = connectTimeoutInMillisecond;
        return this;
    }

    public Integer getMsgTimeoutInMillisecond() {
        return msgTimeoutInMillisecond;
    }

    public AbstractNSQClientInitializer<T> setMsgTimeoutInMillisecond(Integer msgTimeoutInMillisecond) {
        this.msgTimeoutInMillisecond = msgTimeoutInMillisecond;
        return this;
    }
}
