package com.youzan.sz.nsq;

import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQClient implements NSQClient, LinkedAroundHandler {
    protected final Logger                           logger         = LoggerFactory.getLogger(getClass());

    protected final static LinkedList<AroundHandler> handlers       = new LinkedList<>();

    protected static final String                    DEFAULT_LOOKUP = PropertiesUtils
        .getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host", "nsq-qa.s.qima-inc.com:4161");
    /**连接超时时间*/
    protected NSQCodec                               nsqCodec;
    private AbstractNSQClientInitializer             nsqClientInitializer;

    @Override
    public NSQClient register() {
        nsqClientInitializer = init();
        nsqCodec = nsqClientInitializer.getNsqCodec();
        if (StringUtil.isEmpty(nsqClientInitializer.getNsqConfig().getLookupAddresses())) {
            logger.debug("nsq is null,use default set:{}", DEFAULT_LOOKUP);
            nsqClientInitializer.getNsqConfig().setLookupAddresses(DEFAULT_LOOKUP);
        }
        Integer connectionTimeout = Integer.valueOf(PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH,
            "nsq.connection.timeout.second", String.valueOf(TimeUnit.SECONDS.toMillis(10))));
        nsqClientInitializer.getNsqConfig().setConnectTimeoutInMillisecond(connectionTimeout);
        //
        // 设置Netty里的ThreadPoolSize(带默认值): 1Thread-to-1IOThread, 使用BlockingIO
        nsqClientInitializer.getNsqConfig().setThreadPoolSize4IO(2);
        // 设置timeout(带默认值): 一次IO来回+本机执行了返回给client code完成的消耗时间
        nsqClientInitializer.getNsqConfig().setConnectTimeoutInMillisecond(3*1000);
        // 设置message中client-server之间可以的timeout(带默认值)
        nsqClientInitializer.getNsqConfig().setMsgTimeoutInMillisecond(60 * 1000);

        return this;
    }

    public NSQConfig getNSQConfig() {
        return nsqClientInitializer.getNsqConfig();
    }
    public String getTopic() {
        return nsqClientInitializer.getTopic();
    }
    @Override
    public AroundHandler addLast(AroundHandler AroundHandler) {
        handlers.addLast(AroundHandler);
        return this;
    }

    @Override
    public Object preHandle(Object t) {
        for (AroundHandler handler : handlers) {
            handler.preHandle(t);
        }
        return t;
    }

    @Override
    public Object doHandle(Object o) {
        for (AroundHandler handler : handlers) {
            handler.doHandle(o);
        }
        return o;
    }

    @Override
    public Object postHandle(Object o) {
        for (AroundHandler handler : handlers) {
            handler.postHandle(o);
        }
        return o;
    }

    @Override
    public NSQClient reconnection() {
        return null;
    }

    public int getNextConsumingSecond() {
        return 3;
    }

}
