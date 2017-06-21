package com.youzan.sz.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by mingle.
 * Time 2017/6/20 下午7:54
 * Desc 用于标识carmen需要使用到的字段并带上字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CarmenField {
    Class clazz();
}
