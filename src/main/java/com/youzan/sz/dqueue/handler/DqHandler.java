package com.youzan.sz.dqueue.handler;

/**
 * Created by wangpan on 2016/9/30.
 */
public interface DqHandler {

    /**
     * handler
     * @param <T>
     * @return
     */
    <T,V> T handler(String key,V  v);
}
