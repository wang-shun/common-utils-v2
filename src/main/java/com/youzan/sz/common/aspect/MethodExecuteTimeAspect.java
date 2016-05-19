package com.youzan.sz.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by Kid on 16/5/17.
 */

public class MethodExecuteTimeAspect extends BaseAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodExecuteTimeAspect.class);

    private int timeout = 30;

    public MethodExecuteTimeAspect() {
    }

    public MethodExecuteTimeAspect(int timeout) {
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

        try {
            return pjp.proceed();
        } finally {
            long executeTime = System.currentTimeMillis() - beginTime;
            if (executeTime > timeout) {
                LOGGER.info("Method {}.{} Executed Time (ms):{}", method.getDeclaringClass().getCanonicalName(),
                        method.getName(), executeTime);
            }
        }
    }

}
