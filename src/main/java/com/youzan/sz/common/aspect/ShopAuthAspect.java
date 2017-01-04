package com.youzan.sz.common.aspect;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Auth;
import com.youzan.sz.common.annotation.ShopAuth;
import com.youzan.sz.common.model.auth.GrantPolicyDTO;
import com.youzan.sz.common.permission.PermEnum;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.service.AuthService;

/**
 * Created by wangpan. Time 03/01/2017 5:45 PM Desc 文件描述
 */
public class ShopAuthAspect extends BaseAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopAuthAspect.class);

    @Resource
    private AuthService         authService;

    @Pointcut("@annotation(com.youzan.sz.common.annotation.ShopAuth)")
    public void pointcut() {
    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    }

    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        Method method = this.getMethod(pjp);
        ShopAuth shopAuth = method.getAnnotation(ShopAuth.class);
        Class<?> returnType = method.getReturnType();
        Auth auth = method.getAnnotation(Auth.class);
        // 获取注解上传过来的参数
        PermEnum[] allowedPermissions = auth.allowedPerms();
        long needAppId = shopAuth.appId();
        boolean hasShopPerms = checkPermission(needAppId, allowedPermissions,
            method.getDeclaringClass().getName() + "." + method.getName());
        if (hasShopPerms) {
            return proceed(pjp, returnType);
        } else {
            // 未通过鉴权
            if (BaseResponse.class.isAssignableFrom(returnType)) {//可能有时候不需要抛出异常
                return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "店铺无权访问", null);
            } else {
                throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "店铺无权访问");
            }
        }
    }

    private boolean checkPermission(long appId, PermEnum[] allowedPermissions, String methodName) {
        LOGGER.info("method ({}) need appId ({}) perm", methodName, appId);
        if (appId == 0) {
            return true;
        }
        if (allowedPermissions == null || allowedPermissions.length == 0) {
            return true;
        }
        /*final Long adminId = DistributedContextTools.getAdminId();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("auth user permission:adminId:{}", adminId);
        }*/

        Long shopId = DistributedContextTools.getShopId();
        if (shopId == null) {//店铺不存在
            LOGGER.warn("shopId can not pass authority,context shopId is empty");
            return false;
        }
        //bid不为空,需要进行bid判断
        Long bid = DistributedContextTools.getBid();
        if (bid == null) { //bid不为空,需要进行bid判断
            LOGGER.warn("bid can not pass authority.context bid is empty");
            return false;
        }
        GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO();
        grantPolicyDTO.setBid(Long.valueOf(bid));
        grantPolicyDTO.setShopId(shopId);
        grantPolicyDTO.setAppId(appId);
        BaseResponse<Long[]> response = authService.loadShopPermission(grantPolicyDTO);
        Long[] shopPermissions = response.getData();
        if (shopPermissions == null || shopPermissions.length == 0) {
            LOGGER.warn("shop permissions is null,bid {} shopId {} method:{}", bid, shopId, methodName);
            return false;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("get shop permissions:bid :{} shopId :{} permission:{}", bid, shopId, shopPermissions);
        }

        for (PermEnum permissionsEnum : allowedPermissions) {
            if ((permissionsEnum.getPermInx().getIndex() + 1) > shopPermissions.length) {
                LOGGER.warn(
                    "interface permissions out of shop permissions,bid :{} shopId :{},need permission:{},shop permissions:{},method:{}",
                    bid, shopId, permissionsEnum, shopPermissions, methodName);
                return false;
            }
            Long needPermission = permissionsEnum.getPermInx().getValue();
            if ((shopPermissions[permissionsEnum.getPermInx().getIndex()] & needPermission) != needPermission) {
                LOGGER.warn(
                    "interface permissions out of shop permissions,bid :{} shopId :{},need permission:{},shop permissions:{},method:{}",
                    bid, shopId, permissionsEnum, Long.toHexString(needPermission), methodName);
                return false;
            }

        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("shop permissions check pass bid :{} shopId :{},permission:{}", bid, shopId, shopPermissions);
        }
        return true;
    }
}
