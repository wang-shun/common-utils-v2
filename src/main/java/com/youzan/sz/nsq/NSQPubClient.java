package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface NSQPubClient extends NSQClient {

    Boolean pub(Object Object);

}
