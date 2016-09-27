package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface AroundHandler<T> {
    default T preHandle(T t) {
        return t;
    }

    T doHandle(T t);

    default T postHandle(T t) {
        return t;
    }
}
