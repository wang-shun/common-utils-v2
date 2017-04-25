package com.youzan.sz.DistributedCallTools;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.filter.ExceptionFilter;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.dubbo.rpc.*;
import com.youzan.sz.common.ext.ExtExceptionFilter;
import com.youzan.sz.common.ext.ExtManager;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.Method;


/**
 * Created by vincentbu.
 * Time 2016/12/15 16:59
 * Desc 用来处理异常问题
 */
@Activate(group = {Constants.PROVIDER}, order = 1)
public class BizExceptionFilter implements Filter {
    
    private final Logger logger;
    
    
    public BizExceptionFilter() {
        this(LoggerFactory.getLogger(ExceptionFilter.class));
    }
    
    
    public BizExceptionFilter(Logger logger) {
        this.logger = logger;
    }
    
    
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
    
        // 有扩展的，走扩展流程，替换原来的处理流程
        ExtExceptionFilter extExceptionFilter = ExtManager.getExtExceptionFilter();
        if(extExceptionFilter != null){
            if(logger.isInfoEnabled()){
                logger.info("invocation:"+ ReflectionToStringBuilder.toString(invocation));
            }
            return extExceptionFilter.invoke(invoker, invocation);
        }
        
        
        try {
            Result result = invoker.invoke(invocation);
            if (result.hasException() && GenericService.class != invoker.getInterface()) {
                try {
                    Throwable exception = result.getException();
                    
                    // 如果是checked异常，直接抛出
                    if (!(exception instanceof RuntimeException) && (exception instanceof Exception)) {
                        return result;
                    }
                    // 在方法签名上有声明，直接抛出
                    try {
                        Method method = invoker.getInterface().getMethod(invocation.getMethodName(), invocation.getParameterTypes());
                        Class<?>[] exceptionClassses = method.getExceptionTypes();
                        for (Class<?> exceptionClass : exceptionClassses) {
                            if (exception.getClass().equals(exceptionClass)) {
                                return result;
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        return result;
                    }
                    
                    // 未在方法签名上定义的异常，在服务器端打印ERROR日志
                    logger.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " +
                            "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + "" + invocation.getMethodName() + ", exception: " + exception.getClass().getName() + ": " +
                            exception.getMessage(), exception);
                    
                    // 异常类和接口类在同一jar包里，直接抛出
                    String serviceFile = ReflectUtils.getCodeBase(invoker.getInterface());
                    String exceptionFile = ReflectUtils.getCodeBase(exception.getClass());
                    if (serviceFile == null || exceptionFile == null || serviceFile.equals(exceptionFile)) {
                        return result;
                    }
                    // 是JDK自带的异常，直接抛出
                    String className = exception.getClass().getName();
                    if (className.startsWith("java.") || className.startsWith("javax.")) {
                        return result;
                    }
                    // 是Dubbo本身的异常，直接抛出
                    if (exception instanceof RpcException) {
                        return result;
                    }
                    //如果是自定义异常,直接抛出
                    if (className.startsWith("com.youzan.sz")) {
                        return result;
                        //                        return new RpcResult(new RuntimeException(exception));
                    }
                    
                    //add by qingjiao.业务异常返回
                    if ("com.youzan.platform.bootstrap.exception.BusinessException".equals(className) || "com.youzan.platform.bootstrap.exception.SystemException".equals(className)) {
                        return result;
                    }
                    
                    // 否则，包装成RuntimeException抛给客户端
                    return new RpcResult(new RuntimeException(StringUtils.toString(exception)));
                } catch (Throwable e) {
                    logger.warn("Fail to ExceptionFilter when called by " + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " + invocation
                            .getMethodName() + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
                    return result;
                }
            }
            return result;
        } catch (RuntimeException e) {
            logger.error("Got unchecked and undeclared exception which called by " + RpcContext.getContext().getRemoteHost() + ". service: " + invoker.getInterface().getName() + ", method: " +
                    invocation.getMethodName() + ", exception: " + e.getClass().getName() + ": " + e.getMessage(), e);
            throw e;
        }
    }
}
