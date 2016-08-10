package com.youzan.sz.nsq;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.MessageOrBuilder;
import com.youzan.sz.common.util.JsonUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class ProtoBufCodec<T extends GeneratedMessage, V extends AbstractMessageLite> extends AbstractNSQCodec<T, V> {
    private final static Map<Class, Method> DECODE_METHODS_MAP = new HashMap<>();// 存储解码方法MAP

    public ProtoBufCodec(Class<T> clazz) {
        super.decodeClazz = clazz;
    }

    public ProtoBufCodec() {
    }

    @Override
    public T decode(byte[] bytes) {

        Method decodeMethod = DECODE_METHODS_MAP.get(getDecodeClass());
        if (decodeMethod == null) {//非线程安全,但是只要能取到method即可,特别地不影响后面性能
            try {
                decodeMethod = getDecodeClass().getMethod("parseFrom", new Class[] { byte[].class });
                DECODE_METHODS_MAP.put(getDecodeClass(), decodeMethod);
            } catch (NoSuchMethodException e) {
                logger.error("", e);
                return null;
            }

        }
        try {
            return (T) decodeMethod.invoke(getDecodeClass(), new Object[] { bytes });
        } catch (Exception e) {
            logger.error("protobuf decode error:", e);
        }
        return null;
    }

    @Override
    public byte[] encode(V v) {
        return v.toByteArray();
    }
}
