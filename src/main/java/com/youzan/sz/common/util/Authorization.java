package com.youzan.sz.common.util;

import com.youzan.sz.oa.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by YANG on 16/4/7.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorization {

    RoleEnum[] allowedRoles();

    String adminId();

    String shopId() default "";

}
