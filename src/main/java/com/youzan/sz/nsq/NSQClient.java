package com.youzan.sz.nsq;

import com.youzan.nsq.client.exception.NSQException;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface NSQClient {

    NSQClient register();

    AbstractNSQClientInitializer init();

    NSQClient reconnection();

}
