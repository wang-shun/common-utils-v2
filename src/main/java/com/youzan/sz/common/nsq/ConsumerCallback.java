package com.youzan.sz.common.nsq;

/**
 * Created by jinxiaofei on 16/7/21.
 */
@FunctionalInterface
public interface ConsumerCallback {
    public <T extends NsqMessage> void callback(T message);
}
