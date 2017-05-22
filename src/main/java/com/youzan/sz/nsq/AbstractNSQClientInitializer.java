package com.youzan.sz.nsq;

import com.google.common.base.Strings;

import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQClientInitializer<T> implements ClientInitializer {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    protected NSQConfig nsqConfig;
    
    protected NSQCodec nsqCodec = null;
    
    protected String topic = null;
    
    protected String consumerName;
    
    protected Integer connectTimeoutInMillisecond = 3 * 1000;
    
    protected Integer msgTimeoutInMillisecond = (int) TimeUnit.SECONDS.toMillis(120);
    
    
    protected AbstractNSQClientInitializer() {
        try {
            
            nsqConfig = new NSQConfig();
        } catch (RuntimeException e) {
            logger.error("init exception", e);
        }
    }
    
    
    @Override
    public void build() {
        final String property = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.connection.timeout.second");
        if (StringUtil.isNoneEmpty(property)) {//如果配置文件有,则使用配置文件
            final Integer integer = Integer.valueOf(property);
            if (logger.isInfoEnabled()) {
                logger.info("user config timeout:{}", integer);
            }
            this.connectTimeoutInMillisecond = integer;
        }
        
        if (StringUtil.isEmpty(this.consumerName)) {
            final String defaultConsumerName = "sz" + "_" + getTopic() + "consumer";
            if (logger.isInfoEnabled()) {
                logger.info("not config consumer name ,use default name:{}", defaultConsumerName);
            }
            this.consumerName = defaultConsumerName;
        }
        
        // if (getNsqConfig().getLookupAddresses() == null || getNsqConfig().getLookupAddresses().length == 0) {
        if (Strings.isNullOrEmpty(getNsqConfig().getLookupAddresses())) {
            logger.debug("nsq is null,use default set:{}", getLookupDefault());
            getNsqConfig().setLookupAddresses(getLookupDefault());
        }
        getNsqConfig().setThreadPoolSize4IO(1);
        getNsqConfig().setConsumerName(getConsumerName());
        getNsqConfig().setConsumerName(consumerName);
        getNsqConfig().setConnectTimeoutInMillisecond(connectTimeoutInMillisecond);
        getNsqConfig().setMsgTimeoutInMillisecond(msgTimeoutInMillisecond);
    }
    
    
    public String getTopic() {
        return this.topic;
    }
    
    
    /**
     * 设置主题
     */
    public AbstractNSQClientInitializer setTopic(String topic) {
        Objects.requireNonNull(topic);
        this.topic = topic;
        return this;
    }
    
    
    public NSQConfig getNsqConfig() {
        return nsqConfig;
    }
    
    
    public String getConsumerName() {
        return consumerName;
    }
    
    
    public AbstractNSQClientInitializer setConsumerName(String consumerName) {
        this.consumerName = consumerName;
        return this;
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
    
    
    public NSQCodec getNsqCodec() {
        return nsqCodec;
    }
    
    
    public AbstractNSQClientInitializer setCodec(NSQCodec nsqCodec) {
        Objects.requireNonNull(nsqCodec);
        this.nsqCodec = nsqCodec;
        return this;
    }
    
    
    public AbstractNSQClientInitializer setLookUp(String... lookUps) {
        if (lookUps != null && lookUps.length > 0) {
            StringBuilder lp = new StringBuilder();
            for (String lookUp : lookUps) {
                lp.append(lookUp).append(",");
            }
            nsqConfig.setLookupAddresses(lp.toString());
        }
        
        return this;
    }
}
