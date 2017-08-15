package com.youzan.sz.common.annotation;

import com.youzan.sz.common.enums.AbilityEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by jinxiaofei.
 * Time 2017/5/12 下午12:42
 * Desc 文件描述
 * 检查店铺能力的类
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Ability {
    
    AbilityEnum biz();
    //不传会从上下文获取，传了会校验传入的kdtId
    long kdtId() default 0L;
}
