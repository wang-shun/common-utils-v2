package com.youzan.sz.common.aspect;

import com.youzan.sz.common.util.JsonUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;


/**
 * Created by mingle.
 * Time 2/26/17 5:01 PM
 * Desc 文件描述
 */
@Aspect
public class ParameterLogAspect extends BaseAspect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterLogAspect.class);
    private static final String NEW_LINE = "\r\n";
    private int timeout = 30;
    
    
    public ParameterLogAspect() {
    }
    
    
    public ParameterLogAspect(int timeout) {
        this.timeout = timeout;
    }
    
    
    public int getTimeout() {
        return timeout;
    }
    
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        Method method = super.getMethod(pjp);
        long beginTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = pjp.proceed();
            return result;
        } finally {
            long executeTime = System.currentTimeMillis() - beginTime;
            String params = JsonUtils.toJson(pjp.getArgs());
            StringBuilder sb = new StringBuilder("Method: ").append(method.getDeclaringClass().getCanonicalName()).append(".").append(method.getName()).append(params);
            sb.append(" times: ").append(executeTime).append(" ms");
            sb.append(NEW_LINE).append(JsonUtils.toJson(result));
            LOGGER.info(sb.toString());
        }
    }
    
    
    public static void main(String[] args) {
        StringBuilder stringBuilder = new StringBuilder("hello");
        stringBuilder.append(NEW_LINE);
        stringBuilder.append("world");
        System.out.println(stringBuilder.toString());
    }
}
