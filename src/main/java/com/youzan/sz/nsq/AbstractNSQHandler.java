package com.youzan.sz.nsq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public abstract class AbstractNSQHandler<T> implements AroundHandler<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public T preHandle(T t) {
        return t;
    }

    @Override
    public abstract T doHandle(T t);

    @Override
    public T postHandle(T t) {
        return t;
    }
}
