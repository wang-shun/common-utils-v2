package com.youzan.sz.common.aspect;

import com.google.common.base.Strings;

import com.alibaba.fastjson.JSON;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.ValidateParams;
import com.youzan.sz.common.model.BaseDTO;
import com.youzan.sz.common.model.BaseModel;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.SpringUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;


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
    
    
    @Pointcut("@annotation(com.youzan.sz.common.annotation.ValidateParams)")
    public void pointcut() {
        
    }
    
    
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //TODO 不知道原因，有@Before Advisor，@Around Advisor 才会执行
    }
    
    
    //检验内部使用传递过来的参数
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        
        if (validator == null) { //TODO Spring加载了两次该类，第一次注入了validator,第二次注入null，暂时采用这样的方式注入
            validator = (Validator) SpringUtils.getBean("validator");
        }
        
        Method method = this.getMethod(pjp);
        ValidateParams validateParamsAnnotation = method.getAnnotation(ValidateParams.class);
        Class<?> returnType = method.getReturnType();
        
        Class[] classes = validateParamsAnnotation.paramClasses();
        Object[] args = pjp.getArgs();
        String[] excludeProperties = validateParamsAnnotation.excludeProperties();
        String[] includeProperties = validateParamsAnnotation.includeProperties();
        //如果注解上面校验参数不写的时候，默认校验所有的类
        if (classes.length == 0) {
            classes = method.getParameterTypes();
        }
        
        
        Set<ConstraintViolation<Object>> constraintSet = validate(classes, args, excludeProperties, includeProperties);
        if (!constraintSet.isEmpty()) {
            String errors = buildErrorMsg(constraintSet);
            if (BaseResponse.class.isAssignableFrom(returnType)) {
                return new BaseResponse<>(ResponseCode.PARAMETER_ERROR.getCode(), errors, null);
            }else {
                throw new BusinessException((long) ResponseCode.PARAMETER_ERROR.getCode(), "参数错误:\t" + errors);
            }
        }else {
            try {
                return pjp.proceed();
            } catch (BusinessException be) {
                LOGGER.warn("Error:", be);
                throw be;
            } catch (Throwable e) {
                LOGGER.warn("Error:", e);
                if (BaseResponse.class.isAssignableFrom(returnType)) {
                    return new BaseResponse<>(ResponseCode.ERROR.getCode(), "系统异常", e);
                }else {
                    throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常", e);
                }
            }
        }
    }
    
    
    /**
     * 构造参数错误消息
     */
    
    private String buildErrorMsg(Set<ConstraintViolation<Object>> constraintSet) {
        
        if (constraintSet != null && !constraintSet.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            constraintSet.forEach(c -> sb.append(c.getPropertyPath()).append(":").append(c.getMessage()).append(","));
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else {
            return "";
        }
    }
    
    
    /**
     * 处理参数,判断是不是符合要求
     */
    private Set<ConstraintViolation<Object>> validate(Class[] classes, Object[] args, String[] excludeProperties,
            String[] includeProperties) {
        
        Set<ConstraintViolation<Object>> constraintSet = new HashSet<>();
        Set<ConstraintViolation<Object>> result = new HashSet<>();
        
        for (Object obj : args) {
            if (obj instanceof Collection) {
                Collection collection = (Collection) obj;
                for (Object o : collection) {
                    setContextArgs(o);
                    constraintSet.addAll(doValidate(classes, o));
                }
            }else {
                constraintSet.addAll(doValidate(classes, obj));
            }
        }
        if (includeProperties != null && includeProperties.length > 0) {
            for (String includeProperty : includeProperties) {
                for (ConstraintViolation<Object> violation : constraintSet) {
                    if (violation.getPropertyPath().toString().equals(includeProperty)) {
                        result.add(violation);
                    }
                }
            }
        }else {
            result = constraintSet;
        }
        
        if (excludeProperties != null) {
            for (String excludeProperty : excludeProperties) {
                result.removeIf(objectConstraintViolation -> objectConstraintViolation.getPropertyPath().toString()
                        .equals(excludeProperty));
            }
        }
        
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Parameters:{}", JSON.toJSONString(args));
        }
        return result;
    }
    
    
    /**
     * 检验
     */
    private Set<ConstraintViolation<Object>> doValidate(Class[] classes, Object obj) {
        
        if (classes != null && classes.length > 0) {
            boolean anyMatch = Arrays.stream(classes).anyMatch(Predicate.isEqual(obj.getClass()));
            if (anyMatch) {
                return validator.validate(obj);
            }
        }
        return Collections.emptySet();
    }
    
    
    /**
     * 给方法参数添加必要的上下文中的参数
     */
    private void setContextArgs(Object argument) {
        
        if (argument instanceof BaseModel) {
            BaseModel baseModel = (BaseModel) argument;
            if (baseModel.getAdminId() == null) {
                baseModel.setAdminId(DistributedContextTools.getAdminId());
            }
            
            if (Strings.isNullOrEmpty(baseModel.getRequestIp())) {
                baseModel.setRequestIp(DistributedContextTools.getRequestIp());
            }
        }else if (argument instanceof BaseDTO) {
            BaseDTO baseDTO = (BaseDTO) argument;
            if (baseDTO.getAdminId() == null) {
                baseDTO.setAdminId(DistributedContextTools.getAdminId());
            }
            
            if (Strings.isNullOrEmpty(baseDTO.getRequestIp())) {
                baseDTO.setRequestIp(DistributedContextTools.getRequestIp());
            }
        }
    }
    
}
