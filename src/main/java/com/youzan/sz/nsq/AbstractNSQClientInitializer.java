package com.youzan.sz.nsq;

import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.platform.util.lang.StringUtil;
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

    protected AbstractNSQClientInitializer() {
        try {
            nsqConfig = new NSQConfig();
        } catch (NSQException e) {
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
        nsqConfig.setTimeoutInSecond(120);
        nsqConfig.setMsgTimeoutInMillisecond((int) TimeUnit.SECONDS.toMillis(120));
        return this;
    }

    public AbstractNSQClientInitializer setTopic(String topic) {
        nsqConfig.setTopic(topic);
        return this;
    }

    public AbstractNSQClientInitializer setCodec(NSQCodec nsqCodec) {
        this.nsqCodec = nsqCodec;
        return this;
    }

    public NSQCodec getNsqCodec() {
        return nsqCodec;
    }

    public NSQConfig getNsqConfig() {
        return nsqConfig;
    }
}
