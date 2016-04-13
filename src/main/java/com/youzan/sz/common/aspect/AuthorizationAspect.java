package com.youzan.sz.common.aspect;

import com.alibaba.dubbo.rpc.RpcContext;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Authorization;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.oa.enums.RoleEnum;
import com.youzan.sz.oa.staff.api.StaffService;
import com.youzan.sz.oa.staff.api.dto.StaffDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by YANG on 16/4/7.
 */

@Aspect
public class AuthorizationAspect extends BaseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationAspect.class);

    @Resource
    private StaffService staffService;

    @Pointcut("@annotation(com.youzan.sz.common.annotation.Authorization)")
    public void pointcut() {
    }


    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        //TODO 不知道原因，有@Before Advisor，@Around Advisor 才会执行
    }

    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        long beginTime = System.currentTimeMillis();
        try {
            //获取拦截到的方法及方法上的注解
            Method method = this.getMethod(pjp);
            Authorization authorization = method.getAnnotation(Authorization.class);
            Class<?> returnType = method.getReturnType();

            // 获取注解上传过来的参数
            RoleEnum[] allowedRoles = authorization.allowedRoles();
            Object adminId = super.parseKey(authorization.adminId(), method, pjp.getArgs());
            Object shopId = super.parseKey(authorization.shopId(), method, pjp.getArgs());

            // 鉴权
            boolean allowAccess;
            try {
                allowAccess = this.allowAccess(allowedRoles, adminId, shopId);
            } catch (Exception e) {
                LOGGER.error("Authorization Exception:{}", e);
                if (BaseResponse.class.isAssignableFrom(returnType)) {
                    return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "无权访问", null);
                } else {
                    throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问该接口,Exception:" + e.getMessage());
                }
            } finally {
                LOGGER.error("完成鉴权所用时间(ms):{}", System.currentTimeMillis() - beginTime);
                beginTime = System.currentTimeMillis();
            }

            if (allowAccess) {
                // 通过鉴权,开始调用业务逻辑方法
                try {
                    return pjp.proceed();
                } catch (Exception e) {
                    LOGGER.error("Exception:{}", e);
                    if (BaseResponse.class.isAssignableFrom(returnType)) {
                        return new BaseResponse(ResponseCode.ERROR.getCode(), e.getMessage(), null);
                    } else {
                        throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常,Exception:" + e.getMessage());
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
        } finally {
            LOGGER.error("纯粹处理业务本身所用时间(ms):{}", System.currentTimeMillis() - beginTime);
        }
    }


    /**
     * 是否允许访问
     *
     * @param allowedRoles
     * @return
     */
    private boolean allowAccess(RoleEnum[] allowedRoles, Object adminId, Object shopId) {
        LOGGER.info("ADMIN_ID:{}, SHOP_ID:{}", adminId, shopId);

        if (adminId == null) {
            adminId = DistributedContextTools.getAdminId();
            if (adminId == null) {
                return false;
            }
        }

        StaffDTO staffDTO = staffService.getStaffByAdminId(adminId.toString());
        if (staffDTO == null) {
            Future<StaffDTO> future = RpcContext.getContext().getFuture();
            try {
                if (future != null) {
                    staffDTO = future.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("Exception:{}", e);
            }
            if (staffDTO == null) {
                return false;
            }
        }

        for (int i = 0; i < allowedRoles.length; i++) {
            if (allowedRoles[i].equals(RoleEnum.valueOf(staffDTO.getRole()))) {
                if (shopId != null && staffDTO.getShopId() != ((Long) shopId)) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        return true;
    }


}
