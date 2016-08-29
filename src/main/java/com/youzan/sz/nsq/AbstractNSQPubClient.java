package com.youzan.sz.nsq;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.sz.common.util.JsonUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQPubClient<V> extends AbstractNSQClient implements NSQPubClient {

    Producer p = null;

    public NSQClient register() {
        super.register();
        try {
            p = new ProducerImplV2(getNSQConfig());
            logger.info("consume prepare start whth configs {},topic {}", JsonUtils.bean2Json(getNSQConfig()),getTopic());
            p.start();
        } catch (NSQException e) {
            logger.error("nsq register fail", e);
        }
        return this;
    }

    @Override
    public boolean pub(Object object) {
        for (AroundHandler handler : handlers) {
            handler.preHandle(object);
            handler.doHandle(object);
            handler.postHandle(object);
        }
        byte[] bytes;
        if (object == null) {
            bytes = new byte[0];
        } else
            bytes = nsqCodec.encode(object);
        try {
            p.publish(bytes,getTopic());
            return true;
        } catch (NSQException e) {
            logger.error("publish message:{} fail", JsonUtils.bean2Json(object), e);

        }

        return false;
    }

}
