package com.youzan.sz.common.nsq;

/**
 * Created by jinxiaofei on 16/7/21.
 */
@FunctionalInterface
public interface ConsumerCallback {
   <T extends NSQmsgBean> void callback(T message);
}
