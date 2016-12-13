package com.youzan.sz.common.annotation;

import com.youzan.sz.common.permission.PermEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangpan on 2016/12/5.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
    PermEnum[] allowedPerms();
}
