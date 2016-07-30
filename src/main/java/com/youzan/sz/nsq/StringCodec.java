package com.youzan.sz.nsq;

import java.nio.charset.StandardCharsets;

import com.youzan.sz.common.util.JsonUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class StringCodec<T extends NSQMsg, V> extends AbstractNSQCodec<T, V> {

    public StringCodec() {
    }

    public StringCodec(Class<T> t) {
        this.decodeClazz = t;
    }

    @Override
    public T decode(byte[] bytes) {
        if (getDecodeClass() == null) {
            logger.error("must set decode class");
            return null;
        }
        if (bytes == null) {
            return null;
        }
        return JsonUtils.json2Bean(new String(bytes, StandardCharsets.UTF_8), getDecodeClass());
    }

    @Override
    public byte[] encode(Object obj) {
        return JsonUtils.bean2Json(obj).getBytes(StandardCharsets.UTF_8);
    }
}
