package com.youzan.sz.test.nsq;

import com.youzan.sz.nsq.*;
import com.youzan.sz.test.nsq.json.DemoStoreMsg;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class DemoPubNsq extends AbstractNSQPubClient {

    @Override
    public AbstractNSQClientInitializer init() {
        return initPBCodec();
    }

    public AbstractNSQClientInitializer initStringCodec() {
        AbstractNSQClientInitializer<DemoStoreMsg> initializer = new NSQPubClientInitializer<DemoStoreMsg>();
        initializer.setTopic("sz_ss_string_demo").setCodec(new StringCodec(DemoStoreMsg.class));
        return initializer;
    }

    public AbstractNSQClientInitializer initPBCodec() {
        AbstractNSQClientInitializer initializer = new NSQPubClientInitializer();
        initializer.setTopic("sz_ss_pb_demo").setCodec(new ProtoBufCodec());
        return initializer;
    }
}
