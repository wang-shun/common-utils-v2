package com.youzan.sz.nsq;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.sz.common.exceptions.AbortionException;
import com.youzan.sz.common.util.JsonUtils;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQConsRClient extends AbstractNSQClient implements NSQConsRClient, MessageHandler {
    
    Consumer consumer;
    
    private ExecutorService executorService = null;
    
    private NSQConsRConfig nsqConsRConfig = null;
    
    ThreadPoolExecutor threadExecutor = null;
    
    
    @Override
    public NSQClient register() {
        super.register();
        consumer = new ConsumerImplV2(getNSQConfig(), this);
        NSQConsRConfig nsqConsRConfig = getNSQConsRConfig();
        
        //使用异步来处理发送消息
        ThreadFactory pushThreadFactory = new BasicThreadFactory.Builder().namingPattern(getNsqClientInitializer().getConsumerName() + "-%d").build();
        threadExecutor = new ThreadPoolExecutor(nsqConsRConfig.getMinThreadCount(), nsqConsRConfig.getMaxThreadCount(), 1L, TimeUnit.MINUTES, new LinkedBlockingQueue(nsqConsRConfig.getQueueSize()),
                pushThreadFactory);
        try {
            consumer.subscribe(getTopic());
            logger.info("consume prepare start with configs:{},topic:{} ", JsonUtils.bean2Json(getNSQConfig()), getTopic());
            consumer.start();
        } catch (NSQException e) {
            logger.error("start ", e);
        }
        return this;
    }
    
    
    protected NSQConsRConfig getNSQConsRConfig() {
        if (nsqConsRConfig == null) {
            this.nsqConsRConfig = new NSQConsRConfig();
        }
        
        return this.nsqConsRConfig;
    }
    
    
    /***
     * 请使用initializer初始化*/
    @Deprecated
    public void setConsumerName(String consumerName) {
        getNsqClientInitializer().setConsumerName(consumerName);
    }
    
    //    public String getConsumerName() {
    //        if (StringUtils.isEmpty(getNsqClientInitializer().getConsumerName())) {
    //            final String defaultConsumerName = "sz" + "_" + init().getTopic() + "consumer";
    //            if (logger.isInfoEnabled()) {
    //                logger.info("not config consumer name ,use default name:{}", defaultConsumerName);
    //            }
    //
    //            getNsqClientInitializer().setConsumerName(defaultConsumerName);
    //        }
    //        return getNsqClientInitializer().getConsumerName();
    //    }
    
    
    @Override
    public void process(NSQMessage message) {
        final long start = System.currentTimeMillis();
        logger.info("start handle message :{}", new String(message.getMessageID()));
        final Object obj = doLoad(message);
        if (obj == null) {//暂时不支持空消息体
            logger.warn("parse msg is null,skip message:{}", message.getMessageID());
        }else {
            doProcessor(message, obj);
        }
        doFinish(message);
        logger.info("handle message end :{},cost:{}", new String(message.getMessageID()), (System.currentTimeMillis() - start));
    }
    
    
    private void doProcessor(NSQMessage message, Object obj) {
        for (AroundHandler handler : handlers) {
            try {
                handler.preHandle(obj);
                handler.doHandle(obj);
                if (message.getNextConsumingInSecond() != null) {//赋值了下一次发送时间,相当于标识提前结束
                    logger.info("message next consume flag set {},abort invoke,message:{}", message.getNextConsumingInSecond(), JsonUtils.toJson(message));
                    break;
                }
                
            } catch (AbortionException ae) {//just swallow this exception
                logger.info("msg:{} had been aborted by:{}", JsonUtils.toJson(obj), handler.getClass().getName());
                break;
            } catch (Throwable e) {
                logger.error("handle message end,cause:", e);
                try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
                    message.setNextConsumingInSecond(getNextConsumingSecond());
                } catch (NSQException e1) {
                    logger.error("nsq set next consume time error", e1);
                }
                break;
            } finally {
                handler.postHandle(obj);
            }
        }
    }
    
    
    /**
     * 装载
     */
    private Object doLoad(NSQMessage message) {
        Object obj = nsqCodec.decode(message.getMessageBody());
        if (logger.isInfoEnabled()) {
            logger.info("parse result:{}", JsonUtils.bean2Json(obj));
        }
        if (message.getNextConsumingInSecond() != null && message.getNextConsumingInSecond() > 0) {
            try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
                if (logger.isInfoEnabled()) {
                    logger.info("start handle message,reset  next consume time:{} to 0,message:{}", message.getNextConsumingInSecond(), JsonUtils.toJson(message));
                }
                message.setNextConsumingInSecond(null);
            } catch (NSQException e1) {
                logger.error("", e1);
            }
        }
        return obj;
    }
    
    
    private void doFinish(NSQMessage message) {
        try {
            consumer.finish(message);
        } catch (NSQException e) {
            logger.error("finish message error", e);
        }
    }
    
    
    //        threadExecutor
    //            .execute(new HandlerMsgCMD(nsqCodec, message, handlers, logger, getNextConsumingSecond(), consumer));
    public static void main(String[] args) {
        String s =
                "eyJoZWFkZXJzIjp7ImFjdGlvbklkIjoiMjkxNGIwZmU5OWRiNDZkYjg0NjJmODMyOTQ3OWNkNmIiLCJ0eElkIjoicmVmdW5kLTE3MDExMjE1NDM0ODM3NDI5MkFQUExZX0ZBSUwifSwiYml6Qm9keSI6eyJyZWZ1bmRSZXN1bHRFbnVtIjoiUkVGVU5EX1NVQ0NFU1MiLCJhY3F1aXJlTm8iOiIxNzAxMTIxNTQyMDYzNzQyODAiLCJvdXRCaXpObyI6IjMwNzI5NlhUMTcwMTEyMDY5NDE2IiwiZXh0SW5mbyI6IntcImFwcFwiOlwieW91emFuY2FzaGllclwiLFwic2hvcElkXCI6XCIxMTAxODRcIixcImJpZFwiOlwiMzA3Mjk2XCJ9In19";
        
    }
}


//class HandlerMsgCMD implements Runnable {
//
//    private NSQCodec            nsqCodec;
//    private NSQMessage          message;
//    private List<AroundHandler> handlers;
//    private Logger              logger;
//    private int                 getNextConsumingInSecond;
//    private Consumer            consumer;
//
//    public HandlerMsgCMD(NSQCodec nsqCodec, NSQMessage message, List<AroundHandler> handlers, Logger logger,
//                         int getNextConsumingInSecond, Consumer consumer) {
//        this.nsqCodec = nsqCodec;
//        this.message = message;
//        this.handlers = handlers;
//        this.logger = logger;
//        this.getNextConsumingInSecond = getNextConsumingInSecond;
//        this.consumer = consumer;
//    }
//
//    @Override
//    public void run() {
//        logger.debug("start handle message :{}", new String(message.getMessageID()));
//        Object obj = nsqCodec.decode(message.getMessageBody());
//        if (message.getNextConsumingInSecond() != null && message.getNextConsumingInSecond() > 0) {
//            try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
//                if (logger.isInfoEnabled()) {
//                    logger.info("start handle message,reset  next consume time to 0,message:{}",
//                        JsonUtils.toJson(message));
//                }
//                message.setNextConsumingInSecond(0);
//            } catch (NSQException e1) {
//                logger.error("", e1);
//            }
//        }
//
//        if (logger.isInfoEnabled()) {
//            logger.info("parse result:{}", JsonUtils.toJson(obj));
//        }
//        for (AroundHandler handler : handlers) {
//            handler.preHandle(obj);
//
//            try {
//                handler.doHandle(obj);
//                if (message.getNextConsumingInSecond() != null) {//赋值了下一次发送时间,相当于标识提前结束
//                    logger.info("message next consunm flag set {},abort invoke,message:{}",
//                        message.getNextConsumingInSecond(), JsonUtils.toJson(message));
//                    break;
//                }
//
//            } catch (Throwable e) {
//                logger.error("handler message end,cause:", e);
//                try {//设置一个下次消费时间.避免这条消息因为异常被误以为处理成功
//                    message.setNextConsumingInSecond(getNextConsumingInSecond);
//                } catch (NSQException e1) {
//                    logger.error("", e1);
//                }
//                handler.postHandle(obj);
//                break;
//            }
//        }
//        try {
//            consumer.finish(message);
//            logger.info("handle message end :{}", new String(message.getMessageID()));
//        } catch (NSQException e) {
//            logger.warn("finish message error", e);
//        }
//    }
//}
