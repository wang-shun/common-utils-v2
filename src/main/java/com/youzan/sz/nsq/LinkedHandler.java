package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface LinkedHandler {
    LinkedHandler addHander(LinkedHandler linkedHandler);

    boolean remove(LinkedHandler linkedHandler);

}
