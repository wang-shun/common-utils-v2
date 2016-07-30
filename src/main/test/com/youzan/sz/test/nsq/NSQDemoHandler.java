package com.youzan.sz.test.nsq;

import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.nsq.AbstractNSQHandler;
import com.youzan.sz.test.nsq.json.DemoStoreMsg;

/**
 *
 * Created by zhanguo on 16/7/30.
 */
public class  NSQDemoHandler extends AbstractNSQHandler<DemoStoreMsg> {
    @Override
    public DemoStoreMsg doHandle(DemoStoreMsg demoStoreMsg) {
        logger.info("consumer msg:{}", JsonUtils.bean2Json(demoStoreMsg));
        return demoStoreMsg;
    }
}
