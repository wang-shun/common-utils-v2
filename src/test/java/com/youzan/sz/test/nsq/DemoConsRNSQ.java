package com.youzan.sz.test.nsq;

import com.youzan.sz.nsq.*;
import com.youzan.sz.test.nsq.protobuf.NSQProtoMsg;

import com.youzan.sz.test.nsq.json.DemoStoreMsg;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class DemoConsRNSQ extends AbstractNSQConsRClient {

    @Override
    public AbstractNSQClientInitializer init() {
        //        return initStringCodec();

        return initStringCodec();

    }

    public AbstractNSQClientInitializer initStringCodec() {
        AbstractNSQClientInitializer<DemoStoreMsg> initializer = new NSQPubClientInitializer<>();
        setConsumerName("我无所谓");
        initializer.setTopic("sz_ss_string_demo").setCodec(new StringCodec(DemoStoreMsg.class));
        this.addLast(new NSQDemoHandler());
        return initializer;
    }

    public AbstractNSQClientInitializer initPBCodec() {
        AbstractNSQClientInitializer<DemoStoreMsg> initializer = new NSQPubClientInitializer<>();
        initializer.setTopic("sz_ss_pb_demo").setCodec(new ProtoBufCodec(NSQProtoMsg.NSQDemoProtoReq.class));
        this.addLast(new NSQDemoHandler());
        return initializer;
    }
}
