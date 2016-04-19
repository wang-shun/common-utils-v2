package com.youzan.sz.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jinxiaofei on 16/4/12.
 *
 * 主要是做日志记录的自动注解
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AddLog {
    String message() default "日志记录";
}
