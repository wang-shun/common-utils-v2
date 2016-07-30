package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface LinkedAroundHandler extends AroundHandler {
    AroundHandler addLast(AroundHandler aroundHandler);

}
