package com.youzan.sz.common.aspect;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.SpringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by YANG on 16/5/17.
 */

@Aspect
public class ValidateFlatParamsAspect extends BaseAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateFlatParamsAspect.class);
    private ExecutableValidator validator;

    @Pointcut("@annotation(com.youzan.sz.common.annotation.ValidateFlatParams)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {

    }

    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        long beginTime = System.currentTimeMillis();

        Method method = this.getMethod(pjp);
        if (validator == null) {
            validator = SpringUtils.getBean(ValidatorFactory.class).getValidator().forExecutables();
        }

        Set<ConstraintViolation<Object>> constraintSet = validator.validateParameters(pjp.getThis(), method, pjp.getArgs());
        if (constraintSet != null && !constraintSet.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            constraintSet.forEach(c ->
                    sb.append(((PathImpl) c.getPropertyPath()).getLeafNode().getName()).append(":").append(c.getMessage()).append(",")
            );
            String errors = sb.deleteCharAt(sb.length() - 1).toString();
            throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), "参数错误:\t" + errors);
        } else {
            try {
                return pjp.proceed();
            } catch (BusinessException be) {
                LOGGER.error("Error:{}", be);
                throw be;
            } catch (Exception e) {
                LOGGER.error("Error:{}", e);
                throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常", e);
            } finally {
                LOGGER.info("整个调用执行时间 (ms):{}", System.currentTimeMillis() - beginTime);
            }
        }
    }

}
