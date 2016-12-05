package com.youzan.sz.common.aspect;

import com.youzan.sz.common.annotation.Auth;
import com.youzan.sz.common.permission.PermissionsEnum;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by wangpan on 2016/12/5.
 */
public class AuthAspect extends BaseAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAspect.class);

    @Pointcut("@annotation(com.youzan.sz.common.annotation.Auth)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    }

    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        Method method = this.getMethod(pjp);
        Auth auth = method.getAnnotation(Auth.class);
        Class<?> returnType = method.getReturnType();

        // 获取注解上传过来的参数
        PermissionsEnum[] allowedPermissions = auth.allowedPermissions();
        return false;
    }

    private boolean checkPermission(PermissionsEnum[] allowedPermissions){


        return false;
    }
}
