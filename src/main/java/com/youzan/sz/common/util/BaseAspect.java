package com.youzan.sz.common.util;

import com.google.common.base.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * Created by YANG on 16/4/7.
 */
public class BaseAspect {

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
}
