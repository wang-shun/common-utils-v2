package com.youzan.sz.nsq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/7/30
 */
public abstract class AbstractNSQCodec<T, V> implements NSQCodec<T, V>, SingleClassDecode<T> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Class<T>     decodeClazz;

    @Override
    public Class<T> getDecodeClass() {
        return decodeClazz;
    }
}
