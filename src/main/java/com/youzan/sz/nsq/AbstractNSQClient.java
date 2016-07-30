package com.youzan.sz.nsq;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.nsq.client.entity.NSQConfig;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQClient implements NSQClient, LinkedAroundHandler {
    protected final Logger                           logger         = LoggerFactory.getLogger(getClass());

    protected final static LinkedList<AroundHandler> handlers       = new LinkedList<>();

    protected static final String                    DEFAULT_LOOKUP = PropertiesUtils
        .getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host", "nsq-qa.s.qima-inc.com:4161");
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
        return this;
    }

    public NSQConfig getNSQConfig() {
        return nsqClientInitializer.getNsqConfig();
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
