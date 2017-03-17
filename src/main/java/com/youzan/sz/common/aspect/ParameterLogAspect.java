package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by mingle.
 * Time 2/26/17 5:01 PM
 * Desc 文件描述
 */
@Aspect
public class ParameterLogAspect extends BaseAspect {
    
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    private static final String NEW_LINE = "\r\n";
    
    private ThreadLocal<List<String>> logLocal = new ThreadLocal<>();
    
    private ThreadLocal<AtomicInteger> countLocal = new ThreadLocal<>();
    
    
    public ParameterLogAspect() {
    }
    
    
    private List getLogList() {
        List<String> logList = logLocal.get();
        if (logList == null) {
            logList = new ArrayList();
            logLocal.set(logList);
        }
        return logList;
    }
    
    
    private AtomicInteger getCountLocal() {
        AtomicInteger count = countLocal.get();
        if (count == null) {
            count = new AtomicInteger(0);
            countLocal.set(count);
        }
        return count;
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        long beginTime = System.currentTimeMillis();
        Object result = null;
        try {
            getCountLocal().incrementAndGet();
            result = pjp.proceed();
            return result;
        } finally {
            //入参
            String params = JsonUtils.toJson(pjp.getArgs());
            StringBuilder sb = new StringBuilder("Call-->: ").append(method.getDeclaringClass().getCanonicalName()).append(".").append(method.getName()).append(params).append(NEW_LINE);
            //计时
            long executeTime = System.currentTimeMillis() - beginTime;
            sb.append(" Times-->: ").append(executeTime).append(" ms").append(NEW_LINE);
            //出参 鉴于出参可能是很长的数组，考虑到占用内存过大问题需要截断
            sb.append(" Return-->: ");
            String resultStr = JsonUtils.toJson(result);
            int strSize = 1000;
            if (resultStr != null && resultStr.length() > strSize) {
                sb.append(resultStr.substring(0, strSize)).append("... ...");
            } else {
                sb.append(resultStr);
            }
            sb.append(NEW_LINE);
            
            sb.append(NEW_LINE);
            getLogList().add(sb.toString());
            //如果已经全部执行完就打印日志
            int count = getCountLocal().decrementAndGet();
            if (count == 0) {
                List<String> list = getLogList();
                for (String str : list) {
                    LOGGER.info(str);
                }
                getLogList().clear();
            }
        }
    }
}
