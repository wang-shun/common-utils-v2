package com.youzan.sz.common.aspect;

import com.youzan.sz.common.annotation.Auth;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by wangpan on 2016/12/5.
 */
@Aspect
public class AuthAspect extends BaseAspect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAspect.class);
    
    //    @Resource
    //    private AuthService authService;
    
    
    @Pointcut("@annotation(com.youzan.sz.common.annotation.Auth)")
    public void pointcut() {
    }
    
    
    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
    }
    
    
    @After("pointcut()")
    public void after(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Auth auth = methodSignature.getMethod().getAnnotation(Auth.class);
        if (auth.clearCache()) {
            //            clearUserPermCatche();
        }
    }
    
    //
    //    private void clearUserPermCatche() {
    //        GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTO();
    //        if (grantPolicyDTO == null) {
    //            return;
    //        }
    //        authService.invalidUserPermission(grantPolicyDTO);
    //    }
    //
    
    //    private GrantPolicyDTO getGrantPolicyDTO() {
    //
    //        Long shopId = DistributedContextTools.getShopId();
    //        if (shopId == null) {//店铺不存在
    //            LOGGER.warn("shopId can not pass authority,context shopId is empty");
    //            return null;
    //        }
    //        //bid不为空,需要进行bid判断
    //        //// TODO: 2016/12/13
    //        Long bid = DistributedContextTools.getBid();
    //        if (bid == null) { //bid不为空,需要进行bid判断
    //            LOGGER.warn("bid can not pass authority.context bid is empty");
    //            return null;
    //        }
    //
    //        String staffId = SessionTools.getInstance().get(SessionTools.STAFF_ID);
    //        if (StringUtil.isEmpty(staffId)) { //bid不为空,需要进行bid判断
    //            LOGGER.warn("staffId can not pass authority.context staffId is empty");
    //            return null;
    //        }
    //        GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO();
    //        grantPolicyDTO.setBid(Long.valueOf(bid));
    //        grantPolicyDTO.setShopId(shopId);
    //        grantPolicyDTO.setStaffId(Long.valueOf(Long.valueOf(staffId)));
    //        return grantPolicyDTO;
    //
    //    }
    //
    //
    //    //检验权限
    //    @Around("pointcut()")
    //    public Object handle(ProceedingJoinPoint pjp) {
    //        Method method = this.getMethod(pjp);
    //        Auth auth = method.getAnnotation(Auth.class);
    //        Class<?> returnType = method.getReturnType();
    //        // 获取注解上传过来的参数
    //        PermEnum[] allowedPermissions = auth.allowedPerms();
    //        boolean allow = checkPermission(allowedPermissions, method.getDeclaringClass().getName() + "." + method.getName());
    //        //检查是否被授予临时权限
    //        if (!allow) {
    //            allow = tryGrant(auth.resource());
    //        }
    //        if (allow) {
    //            // 通过鉴权,开始调用业务逻辑方法
    //            /* try {
    //                return pjp.proceed();
    //            } catch (BusinessException be) {
    //                throw be;
    //            } catch (Throwable e) {
    //                LOGGER.warn("Exception:{}", e);
    //                if (BaseResponse.class.isAssignableFrom(returnType)) {
    //                    return new BaseResponse(ResponseCode.ERROR.getCode(), e.getMessage(), null);
    //                } else {
    //                    throw new BusinessException((long) ResponseCode.ERROR.getCode(), "系统异常", e);
    //                }
    //            }*/
    //            try {
    //                return proceed(pjp, returnType);
    //            } finally {
    //                //记录临时权限消费信息
    //                doClearGrant(auth.resource());
    //            }
    //        }else {
    //            // 未通过鉴权
    //            if (BaseResponse.class.isAssignableFrom(returnType)) {//可能有时候不需要抛出异常
    //                return new BaseResponse(ResponseCode.USER_NO_PERMISSIONS.getCode(), "你无权访问", null);
    //            }else {
    //                throw new BusinessException((long) ResponseCode.USER_NO_PERMISSIONS.getCode(), "你无权访问");
    //            }
    //        }
    //    }
    //
    
    /**
     * 如果有权限 返回true 否则 false
     */
    //    private boolean checkPermission(PermEnum[] allowedPermissions, String name) {
    //
    //        if (allowedPermissions == null || allowedPermissions.length == 0) {
    //            return true;
    //        }
    //
    //        final Long adminId = DistributedContextTools.getAdminId();
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("auth user permission:adminId:{},allowedPermission：{}", adminId, allowedPermissions);
    //        }
    //        GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTO();
    //
    //        if (grantPolicyDTO == null) {
    //            return false;
    //        }
    //
    //        BaseResponse<Long[]> response = authService.loadUserPermission(grantPolicyDTO);
    //
    //        Long[] userPermissions = response.getData();
    //        if (userPermissions == null || userPermissions.length == 0) {
    //            LOGGER.warn("user permissions is null,adminId:{},method:{}", adminId, name);
    //            return false;
    //        }
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("get user permissions:adminId:{},permission:{}", adminId, userPermissions);
    //        }
    //
    //        for (PermEnum permissionsEnum : allowedPermissions) {
    //            if ((permissionsEnum.getPermInx().getIndex() + 1) > userPermissions.length) {
    //                LOGGER.warn("interface permissions out of user owner permissions,adminId:{},need permission:{},owner permissions:{},method:{}", adminId, permissionsEnum, userPermissions, name);
    //                return false;
    //            }
    //            Long needPermission = permissionsEnum.getPermInx().getValue();
    //            if ((userPermissions[permissionsEnum.getPermInx().getIndex()] & needPermission) != needPermission) {
    //                LOGGER.warn("interface permissions out of user owner permissions,adminId:{},need permission:{},owner permissions:{},method:{}", adminId, permissionsEnum, Long.toHexString
    //                        (needPermission), name);
    //                return false;
    //            }
    //
    //        }
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("user permissions check pass :adminId:{},needPermission:{},permission:{}", adminId, allowedPermissions, userPermissions);
    //        }
    //        return true;
    //    }
    //
    //
    //    private GrantPolicyDTO getGrantPolicyDTOWithAdminId() {
    //
    //        GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTO();
    //        grantPolicyDTO.setStaffId(DistributedContextTools.getAdminId());
    //        grantPolicyDTO.setAdminId(DistributedContextTools.getAdminId());
    //
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("getGrantPolicyDTOWithAdminId :adminId:{}", DistributedContextTools.getAdminId());
    //        }
    //        return grantPolicyDTO;
    //
    //    }
    //
    //
    //    /*
    //    * @return 如果临时授权成功,返回true
    //    * */
    //    private boolean tryGrant(ResourceEnum resourceEnum) {
    //
    //        if (resourceEnum == null || resourceEnum == ResourceEnum.NONE) {
    //            if (LOGGER.isInfoEnabled()) {
    //                LOGGER.info("for resource:{} reason ,skip clear grant", resourceEnum);
    //            }
    //            return false;
    //        }
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("resource:({}:{}) 支持提权,开始进行提权检查", resourceEnum.getResource(), resourceEnum.getDesc());
    //        }
    //
    //        final GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTOWithAdminId();
    //        grantPolicyDTO.setResource(resourceEnum.getResource());
    //        final BaseResponse baseResponse = authService.AuthByGrant(grantPolicyDTO);
    //        if (baseResponse.isSucc()) {
    //            return true;
    //        }
    //        if (ResponseCode.PORTAL_GRANT_INFO_EMPTY.getCode() == baseResponse.getCode()) {//未查询到提权信息,统一返回鉴权失败
    //            LOGGER.info("提权尝试失败,未查询到resource:{}提权信息:{}", resourceEnum.getResource(), JsonUtils.toJson(baseResponse));
    //            return false;
    //        }
    //        if (LOGGER.isInfoEnabled()) {
    //            LOGGER.info("提权尝试失败,返回:{}", JsonUtils.toJson(baseResponse));
    //        }
    //        throw new BizException(ResponseCode.getRespondeByCode(baseResponse.getCode()), baseResponse.getData());
    //        //        throw .getBusinessException();
    //    }
    //
    //
    //    private void doClearGrant(ResourceEnum resource) {
    //        if (resource == null || resource == ResourceEnum.NONE) {
    //            if (LOGGER.isInfoEnabled()) {
    //                LOGGER.info("For resource:{} reason ,skip clear grant", resource);
    //            }
    //            return;
    //        }
    //        final GrantPolicyDTO grantPolicyDTO = getGrantPolicyDTOWithAdminId();
    //        grantPolicyDTO.setResource(resource.getResource());
    //        final BaseResponse baseResponse = authService.consumerGrantAuth(grantPolicyDTO);
    //        if (LOGGER.isInfoEnabled())
    //            LOGGER.info("consumer auth|result:{}", JsonUtils.toJson(baseResponse));
    //    }
    //
}
