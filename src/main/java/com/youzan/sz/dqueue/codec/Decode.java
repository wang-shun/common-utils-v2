package com.youzan.sz.dqueue.codec;

/**
 * Created by wangpan on 2016/9/30.
 */
public interface Decode {

    <T> T decode(String reponse, Class<T> z);
}
