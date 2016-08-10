package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface NSQCodec<T, V> {
    T decode(byte[] bytes);

    byte[] encode(V v);

}
