package com.youzan.sz.common.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.youzan.sz.common.enums.RoleEnum;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.model.auth.GrantPolicyDTO;
import com.youzan.sz.common.model.auth.ResourceEnum;
import com.youzan.sz.common.model.base.BaseStaffDTO;
import com.youzan.sz.common.service.AuthService;
import com.youzan.sz.common.util.JsonUtils;
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
import com.youzan.sz.common.annotation.Authorization;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.session.SessionTools;

import javax.annotation.Resource;

/**
 * Created by YANG on 16/4/7.
 */

@Aspect
public class AuthorizationAspect extends BaseAspect {
    private static final Logger LOGGER           = LoggerFactory.getLogger(AuthorizationAspect.class);
    private boolean             ignoreAuthFailed = false;
    @Resource
    private AuthService         authService;

    public boolean isIgnoreAuthFailed() {
        return ignoreAuthFailed;
    }

    public void setIgnoreAuthFailed(boolean ignoreAuthFailed) {
        this.ignoreAuthFailed = ignoreAuthFailed;
    }

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
        final ResourceEnum resource = authorization.resource();

        // 鉴权
        boolean allowAccess;
        try {
            allowAccess = this.allowAccess(allowedRoles, resource, shopId, bid);
        } catch (BizException be) {//如果throw会丢弃掉data数据
            return new BaseResponse(be.getCode().intValue(), be.getMessage(), be.getData());
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            allowAccess = false;
            LOGGER.warn("Authorization Exception:{}", e);
            if (!ignoreAuthFailed) {
                if (BaseResponse.class.isAssignableFrom(returnType)) {
                    return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问", null);
                } else {
                    throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问", e);
                }
            }
        }

        if (allowAccess || ignoreAuthFailed) {
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
            } finally {//为了避免虽然出现异常,但是执行成功(例如调用超时,但是执行成功),调用一次方法就执行一次按次数操作权限
                doClearGrant(allowAccess, resource);
            }
        } else {
            // 未通过鉴权
            if (BaseResponse.class.isAssignableFrom(returnType)) {//可能有时候不需要抛出异常
                return new BaseResponse(ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问", null);
            } else {
                throw new BusinessException((long) ResponseCode.NO_PERMISSIONS.getCode(), "你的角色无权访问");
            }
        }
    }

    /**
     * 是否允许访问
     *
     * @param allowedRoles
     * @param resourceEnum
     * @return
     */
    private boolean allowAccess(RoleEnum[] allowedRoles, ResourceEnum resourceEnum, Object shopId, Object bid) {
        final Long adminId = DistributedContextTools.getAdminId();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("将要进行鉴权:adminId:{},yzAccount:{}", adminId,
                SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT));
        }
        if (shopId != null) {//shopId不为空,需要进行shopId判断
            String userShopId = SessionTools.getInstance().get(SessionTools.SHOP_ID);
            if (!(StringUtil.isNoneEmpty(userShopId) && userShopId.equals(shopId.toString()))) {//店铺不存在或者不相等
                LOGGER.warn("shopId 验证不通过.当前shopId:{},需要shopId:{}", userShopId, shopId);
                return false;
            }
        }
        if (bid != null) {//bid不为空,需要进行bid判断
            String userBid = SessionTools.getInstance().get(SessionTools.BID);
            if (!(StringUtil.isNoneEmpty(userBid) && userBid.equals(bid.toString()))) {
                LOGGER.warn("bid 验证不通过.当前bid:{},需要bid:{}", userBid, bid);
                return false;
            }
        }
        boolean isAllowRole = true;
        if (allowedRoles != null && allowedRoles.length > 0) {
            String roles = SessionTools.getInstance().get(SessionTools.ROLE);
            if (StringUtil.isEmpty(roles) || !Arrays.stream(allowedRoles)
                .anyMatch(roleEnum -> roleEnum.equals(RoleEnum.valueOf(Integer.valueOf(roles))))) {
                LOGGER.error("roles验证不通过,当前roles:{},需要roles:{}", roles, allowedRoles);
                isAllowRole = false;
            }
        }
        if (!isAllowRole && resourceEnum != null && !resourceEnum.equals(ResourceEnum.NONE)) {//如果权限未通过,尝试一下提权
            return tryGrant(resourceEnum);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{}:鉴权成功", adminId);
        }
        return isAllowRole;
    }

    /*
    * @return 如果临时授权成功,返回true
    * */
    private boolean tryGrant(ResourceEnum resourceEnum) {
        if (LOGGER.isInfoEnabled())
            LOGGER.info("resource:({}:{}) 支持提权,开始进行提权检查", resourceEnum.getResource(), resourceEnum.getDesc());
        final BaseStaffDTO baseStaff = getBaseStaff();
        if (baseStaff == null) {
            LOGGER.warn("尝试提权失败,从上下文获取到员工信息失败");
            return false;
        }

        final GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO(baseStaff);
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

    private void doClearGrant(boolean allowAccess, ResourceEnum resource) {
        if (!allowAccess || resource == null || resource == ResourceEnum.NONE) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("For allowAccess :{} or resource:{} reason ,skip clear grant", allowAccess, resource);
            }
            return;
        }
        final BaseStaffDTO baseStaff = getBaseStaff();
        if (baseStaff == null) {
            LOGGER.warn("尝试提权失败|从上下文获取到员工信息失败|直接略过释放:{}", resource);
            return;
        }
        final GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO(baseStaff);
        grantPolicyDTO.setResource(resource.getResource());
        final BaseResponse baseResponse = authService.consumerGrantAuth(grantPolicyDTO);
        if (LOGGER.isInfoEnabled())
            LOGGER.info("consumer auth|result:{}", JsonUtils.toJson(baseResponse));
    }

    private BaseStaffDTO getBaseStaff() {
        final Long bid = DistributedContextTools.getBid();
        final Long shopId = DistributedContextTools.getShopId();
        final Long adminId = DistributedContextTools.getAdminId();
        if (adminId == null || shopId == null || bid == null) {
            LOGGER.warn("数据异常,未从上下文获取到员工信息.adminId:{},bid:{},shopId:{},", adminId, bid, shopId);
            return null;
        }
        final BaseStaffDTO baseStaffDTO = new BaseStaffDTO(adminId, bid, shopId);
        return baseStaffDTO;
    }
}
