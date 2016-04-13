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

    //不传,则从DistributedContext上下文中获取,如果上下文中没有,则返回鉴权失败
    String adminId() default "";

    // 不传,表示不检验操作人与店铺的关系
    String shopId() default "";

}
