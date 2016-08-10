package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/7/30.
 */
public interface SingleClassDecode<T> {
    Class<T> getDecodeClass();

}
