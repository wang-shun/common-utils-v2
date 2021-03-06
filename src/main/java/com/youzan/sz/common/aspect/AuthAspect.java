package com.youzan.sz.common.aspect;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Auth;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.model.auth.GrantPolicyDTO;
import com.youzan.sz.common.model.auth.ResourceEnum;
import com.youzan.sz.common.model.auth.StaffPerm;
import com.youzan.sz.common.permission.PermEnum;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.service.AuthService;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.session.SessionTools;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.annotation.Resource;


/**
 * Created by wangpan on 2016/12/5.
 */
@Aspect
public class AuthAspect extends BaseAspect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAspect.class);
    
    @Resource
    private AuthService authService;
    
    
    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Auth auth = methodSignature.getMethod().getAnnotation(Auth.class);
        if (auth.clearCache()) {
            Long shopId = DistributedContextTools.getShopId();
            Long bid = DistributedContextTools.getBid();
            Long adminId = DistributedContextTools.getAdminId();
            String staffId = SessionTools.getInstance().get(SessionTools.STAFF_ID);
            GrantPolicyDTO grantPolicyDTO = buildGrantPolicyDTO(bid, shopId, Long.valueOf(staffId), adminId);
            if (grantPolicyDTO == null) {
                return;
            }
            try {
                authService.clearStaffPermCache(grantPolicyDTO);
            } catch (Exception e) {
                LOGGER.warn("clear staff perm cache error({}),parameter({})", e, JsonUtils.bean2Json(grantPolicyDTO));
            }
        }
    }
    
    
    private GrantPolicyDTO buildGrantPolicyDTO(Long bid, Long shopId, Long staffId, Long adminId) {
        
        if (shopId == null) {//店铺不存在
            LOGGER.warn("shopId can not pass authority,context shopId is empty");
            shopId = 0L;
        }
        
        if (bid == null) {
            LOGGER.warn("bid can not pass authority.context bid is empty");
            return null;
        }
        
        
        if (null == staffId) {
            LOGGER.warn("staffId can not pass authority.context staffId is empty");
            return null;
        }
        GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO();
        grantPolicyDTO.setBid(bid);
        grantPolicyDTO.setShopId(shopId);
        grantPolicyDTO.setAdminId(adminId);
        grantPolicyDTO.setStaffId(staffId);
        return grantPolicyDTO;
        
    }
    
    
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    }
    
    
    /**
     * 如果有权限 返回true 否则 false
     *
     * @param allowedPermissions
     * @return
     */
    public boolean checkPermission(Long bid, Long shopId, Long staffId, Long adminId, PermEnum... allowedPermissions) {
        
        if (allowedPermissions == null || allowedPermissions.length == 0) {
            return true;
        }
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("auth user permission:adminId:{},staffId:{},allowedPermission：{}", adminId, staffId, allowedPermissions);
        }
        
        
        GrantPolicyDTO grantPolicyDTO = buildGrantPolicyDTO(bid, shopId, staffId, adminId);
        
        if (grantPolicyDTO == null) {
            return false;
        }
        StaffPerm staffPerm = new StaffPerm();
        staffPerm.setKdtId(bid);
        staffPerm.setAdminId(adminId);
        staffPerm.setShopId(shopId);
        if (null != staffId) {
            staffPerm.setStaffId(staffId);
        }else {
            LOGGER.warn("kdtId ({}),adminId({}) staffId is null", staffPerm.getKdtId(), staffPerm.getAdminId());
        }
        staffPerm.setIdx(allowedPermissions[0].getPermInx().getIndex());
        staffPerm.setPos(allowedPermissions[0].getPermInx().getPos());
        staffPerm.setBizS(Arrays.asList(allowedPermissions[0].getBiz().split(",")).stream().filter(biz -> {
            return StringUtil.isNotEmpty(biz);
        }).collect(Collectors.toList()));
        BaseResponse<Boolean> response = authService.checkStaffPerms(staffPerm);
        if (response.getData() == null) {
            return false;
        }
        return response.getData();
        /*Long[] userPermissions = response.getData();
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
            LOGGER.info("user permissions check pass :adminId:{},needPermission:{},permission:{}", adminId,
                allowedPermissions, userPermissions);
        }
        return true;*/
    }
    
    
    private void doClearGrant(ResourceEnum resource) {
        if (resource == null || resource == ResourceEnum.NONE) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("For resource:{} reason ,skip clear grant", resource);
            }
            return;
        }
        final GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTOWithAdminId();
        grantPolicyDTO.setResource(resource.getResource());
        final BaseResponse baseResponse = authService.consumerGrantAuth(grantPolicyDTO);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("consumer auth|result:{}", JsonUtils.toJson(baseResponse));
    }
    
    
    private GrantPolicyDTO getGrantPolicyDTOWithAdminId() {
        
        Long shopId = DistributedContextTools.getShopId();
        Long bid = DistributedContextTools.getBid();
        Long adminId = DistributedContextTools.getAdminId();
        GrantPolicyDTO grantPolicyDTO = buildGrantPolicyDTO(bid, shopId, adminId, adminId);
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("getGrantPolicyDTOWithAdminId :adminId:{}", adminId);
        }
        return grantPolicyDTO;
        
    }
    
    
    //检验权限
    @Around("pointcut()")
    public Object handle(ProceedingJoinPoint pjp) {
        Method method = this.getMethod(pjp);
        Auth auth = method.getAnnotation(Auth.class);
        Class<?> returnType = method.getReturnType();
    
        boolean allow = true;
        
        // 验证注解上的权限信息
        if (null != auth.allowedPerms() && auth.allowedPerms().length > 0) {
            allow = checkPermission(DistributedContextTools.getBid(), DistributedContextTools.getShopId(), Long.valueOf(SessionTools.getInstance().get(SessionTools.STAFF_ID)),
                    DistributedContextTools.getAdminId(), auth.allowedPerms());
        }
        
       
        //检查是否被授予临时权限
        if (!allow) {
            allow = tryGrant(auth.resource());
        }
        if (allow) {
            // 通过鉴权,开始调用业务逻辑方法
            try {
                return proceed(pjp, returnType);
            } finally {
                //记录临时权限消费信息
                doClearGrant(auth.resource());
            }
        }else {
            // 未通过鉴权
            if (BaseResponse.class.isAssignableFrom(returnType)) {//可能有时候不需要抛出异常
                return new BaseResponse(ResponseCode.USER_NO_PERMISSIONS.getCode(), "你无权访问", null);
            }else {
                throw new BusinessException((long) ResponseCode.USER_NO_PERMISSIONS.getCode(), "你无权访问");
            }
        }
    }
    
    
    @Pointcut("@annotation(com.youzan.sz.common.annotation.Auth)")
    public void pointcut() {
    }
    
    
    /*
    * @return 如果临时授权成功,返回true
    * */
    private boolean tryGrant(ResourceEnum resourceEnum) {
    
        if (resourceEnum == null || resourceEnum == ResourceEnum.NONE) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("for resource:{} reason ,skip clear grant", resourceEnum);
            }
            return false;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("resource:({}:{}) 支持提权,开始进行提权检查", resourceEnum.getResource(), resourceEnum.getDesc());
        }
    
        final GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTOWithAdminId();
        grantPolicyDTO.setResource(resourceEnum.getResource());
        final BaseResponse baseResponse = authService.AuthByGrant(grantPolicyDTO);
        if (baseResponse.isSucc()) {
            return true;
        }
        if (ResponseCode.PORTAL_GRANT_INFO_EMPTY.getCode() == baseResponse.getCode()) {//未查询到提权信息,统一返回鉴权失败
            LOGGER.info("提权尝试失败,未查询到resource:{}提权信息:{}", resourceEnum.getResource(), JsonUtils.toJson(baseResponse));
            return false;
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("提权尝试失败,返回:{}", JsonUtils.toJson(baseResponse));
        }
        throw new BizException(ResponseCode.getRespondeByCode(baseResponse.getCode()), baseResponse.getData());
        //        throw .getBusinessException();
    }
    
}
