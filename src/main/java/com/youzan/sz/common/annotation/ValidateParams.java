package com.youzan.sz.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * User: YANG
 * Date: 2015/12/16
 * Time: 13:24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateParams {

    Class[] paramClasses() default {};

    String[] excludeProperties() default {};
    String[] includeProperties() default {};
    
}
