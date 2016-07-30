package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface AroundHandler<T> {
    T preHandle(T t);

    T doHandle(T t);

    T postHandle(T t);
}
