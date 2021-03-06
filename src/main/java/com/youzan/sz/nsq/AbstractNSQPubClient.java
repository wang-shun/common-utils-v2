package com.youzan.sz.nsq;

import com.youzan.nsq.client.Producer;
import com.youzan.nsq.client.ProducerImplV2;
import com.youzan.nsq.client.exception.NSQException;
import com.youzan.sz.common.util.JsonUtils;


/**
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQPubClient extends AbstractNSQClient implements NSQPubClient {
    
    Producer p = null;
    
    
    public NSQClient register() {
        super.register();
        try {
            p = new ProducerImplV2(getNSQConfig());
            logger.info("produce prepare start with configs {},topic {}", JsonUtils.bean2Json(getNSQConfig()), getTopic());
            p.start();
        } catch (NSQException e) {
            logger.error("nsq register fail", e);
        }
        return this;
    }
    
    
    @Override
    public Boolean pub(Object object) {
        for (AroundHandler handler : handlers) {
            handler.preHandle(object);
            handler.doHandle(object);
            handler.postHandle(object);
        }
        byte[] bytes;
        if (object == null) {
            bytes = new byte[0];
        }else {
            if (nsqCodec == null)
                logger.warn("nsq codec is null");
            bytes = nsqCodec.encode(object);
        }
        try {
            p.publish(bytes, getTopic());
            logger.info("push topic({})'s message  {} success", getTopic(), JsonUtils.toJson(object));
            return true;
        } catch (NSQException e) {
            logger.error("publish topic({})'s message:{} fail", getTopic(), JsonUtils.bean2Json(object), e);
            
        }
        
        return false;
    }
    
}
