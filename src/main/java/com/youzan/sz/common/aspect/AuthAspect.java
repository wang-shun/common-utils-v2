package com.youzan.sz.common.aspect;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Auth;
import com.youzan.sz.common.model.auth.GrantPolicyDTO;
import com.youzan.sz.common.permission.PermEnum;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.service.AuthService;
import com.youzan.sz.session.SessionTools;

/**
 * Created by wangpan on 2016/12/5.
 */
@Aspect
public class AuthAspect extends BaseAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAspect.class);

    @Resource
    private AuthService         authService;

    @Pointcut("@annotation(com.youzan.sz.common.annotation.Auth)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    }

    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        Method method = this.getMethod(pjp);
        Auth auth = method.getAnnotation(Auth.class);
        Class<?> returnType = method.getReturnType();
        // 获取注解上传过来的参数
        PermEnum[] allowedPermissions = auth.allowedPerms();
        boolean allow = checkPermission(allowedPermissions,
            method.getDeclaringClass().getName() + "." + method.getName());

        if (allow) {
            // 通过鉴权,开始调用业务逻辑方法
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
        } else {
            // 未通过鉴权
            if (BaseResponse.class.isAssignableFrom(returnType)) {//可能有时候不需要抛出异常
                return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "你无权访问", null);
            } else {
                throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你无权访问");
            }
        }
    }

    /**
     * 如果有权限 返回true 否则 false
     *
     * @param allowedPermissions
     * @return
     */
    private boolean checkPermission(PermEnum[] allowedPermissions, String name) {

        if (allowedPermissions == null || allowedPermissions.length == 0) {
            return true;
        }

        final Long adminId = DistributedContextTools.getAdminId();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("auth user permission:adminId:{}", adminId);
        }

        Long shopId = DistributedContextTools.getShopId();
        if (shopId == null) {//店铺不存在
            LOGGER.warn("shopId can not pass authority,context shopId is empty");
            return false;
        }
        //bid不为空,需要进行bid判断
        //// TODO: 2016/12/13
        Long bid = DistributedContextTools.getBid();
        if (bid == null) { //bid不为空,需要进行bid判断
            LOGGER.warn("bid can not pass authority.context bid is empty");
            return false;
        }

        String staffId = SessionTools.getInstance().get(SessionTools.STAFF_ID);
        if (StringUtil.isEmpty(staffId)) { //bid不为空,需要进行bid判断
            LOGGER.warn("staffId can not pass authority.context staffId is empty");
            return false;
        }
        GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO();
        grantPolicyDTO.setBid(Long.valueOf(bid));
        grantPolicyDTO.setShopId(shopId);
        grantPolicyDTO.setStaffId(Long.valueOf(Long.valueOf(staffId)));
        BaseResponse<Long[]> response = authService.loadUserPermission(grantPolicyDTO);

        Long[] userPermissions = response.getData();
        if (userPermissions == null || userPermissions.length == 0) {
            LOGGER.warn("user permissions is null,adminId:{},method:{}", adminId, name);
            return false;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("get user permissions:adminId:{},permission:{}", adminId, userPermissions);
        }

        for (PermEnum permissionsEnum : allowedPermissions) {
            if ((permissionsEnum.getPermInx().getIndex() + 1) > userPermissions.length) {
                LOGGER.warn(
                    "interface permissions out of user owner permissions,adminId:{},need permission:{},owner permissions:{},method:{}",
                    adminId, permissionsEnum, userPermissions, name);
                return false;
            }
            Long needPermission = permissionsEnum.getPermInx().getValue();
            if ((userPermissions[permissionsEnum.getPermInx().getIndex()] & needPermission) != needPermission) {
                LOGGER.warn(
                    "interface permissions out of user owner permissions,adminId:{},need permission:{},owner permissions:{},method:{}",
                    adminId, permissionsEnum, Long.toHexString(needPermission), name);
                return false;
            }

        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("user permissions check pass :adminId:{},permission:{}", adminId, userPermissions);
        }
        return true;
    }

}
