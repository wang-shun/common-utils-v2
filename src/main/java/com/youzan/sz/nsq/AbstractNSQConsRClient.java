package com.youzan.sz.nsq;

import com.youzan.nsq.client.Consumer;
import com.youzan.nsq.client.ConsumerImplV2;
import com.youzan.nsq.client.MessageHandler;
import com.youzan.nsq.client.entity.NSQMessage;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.sz.common.util.JsonUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQConsRClient extends AbstractNSQClient implements NSQConsRClient, MessageHandler {
    Consumer consumer;

    @Override
    public NSQClient register() {
        super.register();
        getNSQConfig().setConsumerName(getConsumerName());
        consumer = new ConsumerImplV2(getNSQConfig(), this);
        try {
            logger.info("consume prepare start whth configs:{}", JsonUtils.bean2Json(getNSQConfig()));

            consumer.start();
        } catch (NSQException e) {
            logger.error("start ");
        }
        return this;
    }

    public String getConsumerName() {
        return init().getNsqConfig().getTopic() + "-" + "consumer";
    }

    @Override
    public void process(NSQMessage message) {
        Object obj = nsqCodec.decode(message.getMessageBody());
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
        } catch (NSQException e) {
            logger.error("finish message error", e);
        }
    }

}
