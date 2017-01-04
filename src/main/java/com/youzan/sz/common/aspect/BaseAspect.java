package com.youzan.sz.common.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.google.common.base.Strings;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 * Created by YANG on 16/4/7.
 */
public class BaseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAspect.class);
    protected Method getMethod(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        return methodSignature.getMethod();
    }


    //解析SPEL
    protected Object parseKey(String key, Method method, Object[] args) {
        if (Strings.isNullOrEmpty(key)) {
            return null;
        } else {
            LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
            String[] paramNameArray = u.getParameterNames(method);
            SpelExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();

            for (int i = 0; i < paramNameArray.length; ++i) {
                context.setVariable(paramNameArray[i], args[i]);
            }
            return parser.parseExpression(key).getValue(context);
        }
    }

    protected Object proceed(ProceedingJoinPoint pjp, Class<?> returnType) {
        try {
            return pjp.proceed();
        } catch (BusinessException be) {
            throw be;
        } catch (Throwable e) {
            LOGGER.warn("Exception:{}", e);
            if (BaseResponse.class.isAssignableFrom(returnType)) {
                return new BaseResponse(ResponseCode.ERROR.getCode(), e.getMessage(), null);
            } else {
                throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常", e);
            }
        }

    }
}
