package com.youzan.sz.common.util;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.model.BaseDTO;
import com.youzan.sz.common.model.BaseModel;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Kid
 * Date: 2015/12/15
 * Time: 19:06
 */

@Aspect
public class ValidateParamsAspect extends BaseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateParamsAspect.class);


    @Resource //TODO 注入两次,第二次注入null
    private Validator validator;

    @Pointcut("@annotation(com.youzan.sz.common.util.ValidateParams)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //TODO 不知道原因，有@Before Advisor，@Around Advisor 才会执行
    }

    //检验内部使用传递过来的参数
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {

        long beginTime = System.currentTimeMillis();

        if (validator == null) { //TODO Spring加载了两次该类，第一次注入了validator,第二次注入null，暂时采用这样的方式注入
            validator = (Validator) SpringUtils.getBean("validator");
        }


        Method method = this.getMethod(pjp);
        ValidateParams validateParamsAnnotation = method.getAnnotation(ValidateParams.class);
        Class<?> returnType = method.getReturnType();

        Class[] classes = validateParamsAnnotation.paramClasses();
        Object[] args = pjp.getArgs();
        String[] excludeProperties = validateParamsAnnotation.excludeProperties();

        try {
            Set<ConstraintViolation<Object>> constraintSet = validate(classes, args, excludeProperties);
            if (!constraintSet.isEmpty()) {
                String errors = buildErrorMsg(constraintSet);
                if (BaseResponse.class.isAssignableFrom(returnType)) {
                    return new BaseResponse(ResponseCode.PARAMETER_ERROR.getCode(), errors, null);
                } else {
                    throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), "参数错误:" + errors);
                }
            } else {
                try {
                    return pjp.proceed();
                } catch (BusinessException be) {
                    LOGGER.error("Error:{}", be);
                    if (BaseResponse.class.isAssignableFrom(returnType)) {
                        return new BaseResponse(be.getCode().intValue(), be.getMessage(), null);
                    } else {
                        throw be;
                    }
                }
            }
        } finally {
            if (BaseResponse.class.isAssignableFrom(returnType)) {
                LOGGER.error("整个调用执行时间 (ms):{}", System.currentTimeMillis() - beginTime);
            } else {
                LOGGER.error("调用ServiceImpl的方法所用时间 (ms):{}", System.currentTimeMillis() - beginTime);
            }
        }
    }

    /**
     * 构造参数错误消息
     *
     * @param constraintSet
     * @return
     */

    private String buildErrorMsg(Set<ConstraintViolation<Object>> constraintSet) {
        StringBuilder sb = new StringBuilder();
        for (ConstraintViolation<Object> constraint : constraintSet) {
            sb.append(constraint.getPropertyPath()).append(" ：").append(constraint.getMessage()).append(",");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    /**
     * 处理参数,判断是不是符合要求
     *
     * @param classes
     * @param args
     * @return
     */
    private Set<ConstraintViolation<Object>> validate(Class[] classes, Object[] args, String[] excludeProperties) {
        Set<ConstraintViolation<Object>> constraintSet = new HashSet<ConstraintViolation<Object>>();
        for (Object obj : args) {
            if (obj instanceof BaseModel) {
                BaseModel baseModel = (BaseModel) obj;
                if (baseModel.getAdminId() == null) {
                    baseModel.setAdminId(DistributedContextTools.getAdminId());
                }

                if (Strings.isNullOrEmpty(baseModel.getRequestIp())) {
                    baseModel.setRequestIp(DistributedContextTools.getRequestIp());
                }
            } else if (obj instanceof BaseDTO) {
                BaseDTO baseDTO = (BaseDTO) obj;
                if (baseDTO.getAdminId() == null) {
                    baseDTO.setAdminId(DistributedContextTools.getAdminId());
                }

                if (Strings.isNullOrEmpty(baseDTO.getRequestIp())) {
                    baseDTO.setRequestIp(DistributedContextTools.getRequestIp());
                }

            }

            if (obj instanceof Collection) {
                Collection collection = (Collection) obj;
                for (Object object : collection) {
                    constraintSet.addAll(validator.validate(object));
                }
            } else {
                for (Class clazz : classes) {
                    if (clazz.equals(obj.getClass())) {
                        constraintSet.addAll(validator.validate(obj));
                        break;
                    }
                }
            }
        }

        if (excludeProperties != null) {
            for (String excludeProperty : excludeProperties) {
                for (ConstraintViolation<Object> violation : constraintSet) {
                    if (violation.getPropertyPath().toString().equals(excludeProperty)) {
                        constraintSet.remove(violation);
                        break;
                    }
                }
            }
        }

        LOGGER.info("Parameters:{}", JSON.toJSONString(args));

        return constraintSet;
    }

}
