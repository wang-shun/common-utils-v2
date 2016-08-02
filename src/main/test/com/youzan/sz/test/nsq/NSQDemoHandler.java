package com.youzan.sz.test.nsq;

import com.alibaba.fastjson.JSON;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.nsq.AbstractNSQHandler;
import com.youzan.sz.test.nsq.json.DemoStoreMsg;
import com.youzan.sz.test.nsq.protobuf.NSQProtoMsg;

/**
 *
 * Created by zhanguo on 16/7/30.
 */
public class NSQDemoHandler extends AbstractNSQHandler {
    @Override
    public Object doHandle(Object demoStoreMsg) {
        if (demoStoreMsg instanceof NSQProtoMsg.NSQDemoProtoReq) {
            NSQProtoMsg.NSQDemoProtoReq msg = (NSQProtoMsg.NSQDemoProtoReq) demoStoreMsg;
            logger.info("consum msg:{}", JSON.toJSON(msg));
        } else {
            logger.info("consumer msg:{}", JSON.toJSON(demoStoreMsg));
        }

        return demoStoreMsg;
    }
}
