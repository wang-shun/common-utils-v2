package com.youzan.sz.nsq;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQConsRClient extends AbstractNSQClient implements NSQConsRClient, MessageHandler {
    Consumer                consumer;
    private ExecutorService executorService = null;
    private NSQConsRConfig  nsqConsRConfig  = null;
    ThreadPoolExecutor      threadExecutor  = null;
    protected String      CONSUMER_NAME = PropertiesUtils
            .getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.consumer.name", "");
    @Override
    public NSQClient register() {
        super.register();

        getNSQConfig().setConsumerName(getConsumerName());
        consumer = new ConsumerImplV2(getNSQConfig(), this);

        NSQConsRConfig nsqConsRConfig = getNSQConsRConfig();

        //使用异步来处理发送消息
        ThreadFactory pushThreadFactory = new BasicThreadFactory.Builder().namingPattern(getConsumerName() + "-%d")
            .build();
        threadExecutor = new ThreadPoolExecutor(nsqConsRConfig.getMinThreadCount(), nsqConsRConfig.getMaxThreadCount(),
            1L, TimeUnit.MINUTES, new LinkedBlockingQueue(nsqConsRConfig.getQueueSize()), pushThreadFactory);
        try

        {
            consumer.subscribe(getTopic());
            logger.info("consume prepare start whth configs:{},topic {} ", JsonUtils.bean2Json(getNSQConfig()),getTopic());
            consumer.start();
        } catch (NSQException e) {
            logger.error("start ");
        }
        return this;
    }

    protected NSQConsRConfig getNSQConsRConfig() {
        if (nsqConsRConfig == null) {
            this.nsqConsRConfig = new NSQConsRConfig();
        }

        return this.nsqConsRConfig;
    }

    public void setConsumerName(String consumerName) {
        if (StringUtils.isEmpty(consumerName)) {
            Random rd = new Random();
            this.CONSUMER_NAME = init().getTopic() +"_"+ "consuemer"+"_"+(rd.nextDouble()*100+10);
        }else {
            this.CONSUMER_NAME = consumerName;
        }
    }
    public String getConsumerName() {
        if(StringUtils.isEmpty(this.CONSUMER_NAME)){
            Random rd = new Random();
            this.CONSUMER_NAME = init().getTopic() +"_"+ "consuemer"+"_"+(rd.nextDouble()*100+10);
        }
        return this.CONSUMER_NAME;
    }

    @Override
    public void process(NSQMessage message) {

        logger.debug("start handle message :{}", new String(message.getMessageID()));
        Object obj = nsqCodec.decode(message.getMessageBody());
        if (logger.isDebugEnabled()) {
            logger.debug("parse result:{}", JsonUtils.bean2Json(obj));
        }
        for (AroundHandler handler : handlers) {
            handler.preHandle(obj);

            try {
                handler.doHandle(obj);
                if (message.getNextConsumingInSecond() != null) {//赋值了下一次发送时间,相当于标识提前结束
                    logger.info("message next consunm flag set {},abort invoke", message.getNextConsumingInSecond());
                    break;
                }

            } catch (Throwable e) {
                logger.error("hander message end,cause:", e);
                try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
                    message.setNextConsumingInSecond(getNextConsumingSecond());
                } catch (NSQException e1) {
                    logger.error("", e1);
                }
                handler.postHandle(obj);
                break;
            }
        }
        try {
            consumer.finish(message);
            logger.debug("handle message end :{}", new String(message.getMessageID()));
        } catch (NSQException e) {
            logger.error("finish message error", e);
        }
    }

    //        threadExecutor
    //            .execute(new HandlerMsgCMD(nsqCodec, message, handlers, logger, getNextConsumingSecond(), consumer));

}

class HandlerMsgCMD implements Runnable {

    private NSQCodec            nsqCodec;
    private NSQMessage          message;
    private List<AroundHandler> handlers;
    private Logger              logger;
    private int                 getNextConsumingInSecond;
    private Consumer            consumer;

    public HandlerMsgCMD(NSQCodec nsqCodec, NSQMessage message, List<AroundHandler> handlers, Logger logger,
                         int getNextConsumingInSecond, Consumer consumer) {
        this.nsqCodec = nsqCodec;
        this.message = message;
        this.handlers = handlers;
        this.logger = logger;
        this.getNextConsumingInSecond = getNextConsumingInSecond;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        logger.debug("start handle message :{}", new String(message.getMessageID()));
        Object obj = nsqCodec.decode(message.getMessageBody());
        if (logger.isDebugEnabled()) {
            logger.debug("parse result:{}", JsonUtils.bean2Json(obj));
        }
        for (AroundHandler handler : handlers) {
            handler.preHandle(obj);

            try {
                handler.doHandle(obj);
                if (message.getNextConsumingInSecond() != null) {//赋值了下一次发送时间,相当于标识提前结束
                    logger.info("message next consunm flag set {},abort invoke", message.getNextConsumingInSecond());
                    break;
                }

            } catch (Throwable e) {
                logger.error("hander message end,cause:", e);
                try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
                    message.setNextConsumingInSecond(getNextConsumingInSecond);
                } catch (NSQException e1) {
                    logger.error("", e1);
                }
                handler.postHandle(obj);
                break;
            }
        }
        try {
            consumer.finish(message);
            logger.debug("handle message end :{}", new String(message.getMessageID()));
        } catch (NSQException e) {
            logger.error("finish message error", e);
        }
    }
}
