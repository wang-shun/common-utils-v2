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

        return initPBCodec();

    }

    public AbstractNSQClientInitializer initStringCodec() {
        AbstractNSQClientInitializer<DemoStoreMsg> initializer = new NSQPubClientInitializer<>();

        initializer.setTopic("store-sz-string-demo").setCodec(new StringCodec(DemoStoreMsg.class));
        this.addLast(new NSQDemoHandler());
        return initializer;
    }

    public AbstractNSQClientInitializer initPBCodec() {
        AbstractNSQClientInitializer<DemoStoreMsg> initializer = new NSQPubClientInitializer<>();
        initializer.setTopic("store-sz-pb-demo").setCodec(new ProtoBufCodec(NSQProtoMsg.NSQDemoProtoReq.class));
        this.addLast(new NSQDemoHandler());
        return initializer;
    }
}
