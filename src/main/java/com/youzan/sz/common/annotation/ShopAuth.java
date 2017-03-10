package com.youzan.sz.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangpan. Time 03/01/2017 5:46 PM Desc 文件描述
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShopAuth {
    long appId() default 0L;
}
