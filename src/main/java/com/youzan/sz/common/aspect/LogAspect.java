package com.youzan.sz.common.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by jinxiaofei on 16/4/12.
 * 处理日志的切面;
 */
@Component
@Aspect
public class LogAspect {
    private final static Logger logger= LoggerFactory.getLogger("有赞深圳日志记录器");
    @Pointcut("@annotation(com.youzan.sz.common.annotation.AddLog)")
    private void  logAspect(){};

    @Around(value = "logAspect()")
    public Object aroundAdivce(ProceedingJoinPoint pjp) throws Throwable{
        StringBuffer sb=new StringBuffer("");
        MethodSignature signature=(MethodSignature)pjp.getSignature();
        String method=signature.getMethod().toString();
        sb.append("操作方法:").append(method+",参数是:");
        Object[] args=pjp.getArgs();
        for (Object o:args){
            sb.append(o.toString()+",");
        }
        logger.info(sb.toString());
        Object o=pjp.proceed();
        return o;
    }

    /**
     * 核心业务逻辑调用异常退出后，执行此Advice，处理错误信息
     *
     * 注意：执行顺序在Around Advice之后
     * @param joinPoint
     * @param ex
     */
    @AfterThrowing(value = "logAspect()", throwing = "ex")
    public void afterThrowingAdvice(JoinPoint joinPoint, Exception ex) {
       logger.error("出现异常:",ex);
    }
    }


