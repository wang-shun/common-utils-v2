package com.youzan.sz.common.aspect;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.annotation.Authorization;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.oa.enums.RoleEnum;
import com.youzan.sz.session.SessionTools;
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

/**
 * Created by YANG on 16/4/7.
 */

@Aspect
public class AuthorizationAspect extends BaseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationAspect.class);

    @Pointcut("@annotation(com.youzan.sz.common.annotation.Authorization)")
    public void pointcut() {
    }


    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //TODO 不知道原因，有@Before Advisor，@Around Advisor 才会执行
    }

    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        //获取拦截到的方法及方法上的注解
        Method method = this.getMethod(pjp);
        Authorization authorization = method.getAnnotation(Authorization.class);
        Class<?> returnType = method.getReturnType();

        // 获取注解上传过来的参数
        RoleEnum[] allowedRoles = authorization.allowedRoles();
//        Object adminId = super.parseKey(authorization.adminId(), method, pjp.getArgs());
        Object[] args = pjp.getArgs();
        Object shopId = super.parseKey(authorization.shopId(), method, args);
        Object bid = super.parseKey(authorization.bid(), method, args);

        // 鉴权
        boolean allowAccess;
        try {
            allowAccess = this.allowAccess(allowedRoles, shopId, bid);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            LOGGER.error("Authorization Exception:{}", e);
            if (BaseResponse.class.isAssignableFrom(returnType)) {
                return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "无权访问", null);
            } else {
                throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问该接口", e);
            }
        }

        if (allowAccess) {
            // 通过鉴权,开始调用业务逻辑方法
            try {
                return pjp.proceed();
            } catch (BusinessException be) {
                throw be;
            } catch (Throwable e) {
                LOGGER.error("Exception:{}", e);
                if (BaseResponse.class.isAssignableFrom(returnType)) {
                    return new BaseResponse(ResponseCode.ERROR.getCode(), e.getMessage(), null);
                } else {
                    throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常", e);
                }
            }
        } else {
            // 未通过鉴权
            if (BaseResponse.class.isAssignableFrom(returnType)) {
                return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "无权访问", null);
            } else {
                throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问该接口");
            }
        }
    }


    /**
     * 是否允许访问
     *
     * @param allowedRoles
     * @return
     */
    private boolean allowAccess(RoleEnum[] allowedRoles, Object shopId, Object bid) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Parameters: bid:{}, shopId:{}", bid, shopId);
        }

        if (shopId != null && !String.valueOf(shopId).equals(SessionTools.getInstance().get(SessionTools.SHOP_ID))) {
            return false;
        } else if (bid != null && !String.valueOf(bid).equals(SessionTools.getInstance().get(SessionTools.BID))) {
            return false;
        } else {
            boolean success = Arrays.stream(allowedRoles).anyMatch(roleEnum ->
                    roleEnum.equals(RoleEnum.valueOf(Integer.valueOf(SessionTools.getInstance().get(SessionTools.ROLE)))));
            return success;
        }
    }


}
