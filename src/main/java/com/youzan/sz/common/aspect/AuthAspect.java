package com.youzan.sz.common.aspect;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Auth;
import com.youzan.sz.common.model.auth.GrantPolicyDTO;
import com.youzan.sz.common.permission.PermissionsEnum;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.service.AuthService;
import com.youzan.sz.session.SessionTools;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.lang.reflect.Method;

/**
 * Created by wangpan on 2016/12/5.
 */
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
        PermissionsEnum[] allowedPermissions = auth.allowedPermissions();
        boolean allow = checkPermission(allowedPermissions);

        return false;
    }

    /**
     * 如果有权限 返回true 否则 false
     * @param allowedPermissions
     * @return
     */
    private boolean checkPermission(PermissionsEnum[] allowedPermissions) {

        if (allowedPermissions == null || allowedPermissions.length == 0) {
            return true;
        }

        final Long adminId = DistributedContextTools.getAdminId();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("auth user permission:adminId:{},yzAccount:{}", adminId,
                SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT));
        }

        String userShopId = SessionTools.getInstance().get(SessionTools.SHOP_ID);
        if (StringUtil.isEmpty(userShopId)) {//店铺不存在
            LOGGER.warn("shopId can not pass authority,context shopId is empty");
            return false;
        }
        //bid不为空,需要进行bid判断
        String userBid = SessionTools.getInstance().get(SessionTools.BID);
        if (StringUtil.isEmpty(userBid)) { //bid不为空,需要进行bid判断
            LOGGER.warn("bid can not pass authority.context bid is empty");
            return false;
        }

        String staffId = SessionTools.getInstance().get(SessionTools.STAFF_ID);
        if (StringUtil.isEmpty(staffId)) { //bid不为空,需要进行bid判断
            LOGGER.warn("staffId can not pass authority.context staffId is empty");
            return false;
        }
        GrantPolicyDTO grantPolicyDTO = new GrantPolicyDTO();
        grantPolicyDTO.setBid(Long.valueOf(userBid));
        grantPolicyDTO.setShopId(Long.valueOf(Long.valueOf(userShopId)));
        grantPolicyDTO.setStaffId(Long.valueOf(Long.valueOf(staffId)));
        BaseResponse<Long[]> response = authService.loadUserPermission(grantPolicyDTO);

        Long[] userPermissions = response.getData();
        if (userPermissions == null || userPermissions.length == 0) {
            LOGGER.warn("user permissons is null,adminId:{},yzAccount:{}", adminId,
                SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT));
            return false;
        }else {
            if(LOGGER.isInfoEnabled()){
                LOGGER.info("get user permissons:adminId:{},yzAccount:{},permission:{}", adminId,
                        SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT),userPermissions);
            }
        }
        for (PermissionsEnum permissionsEnum : allowedPermissions) {
            if ( (permissionsEnum.getPermissionsIndex().getIndex()+1) > userPermissions.length) {
                LOGGER.warn("interface permissons out of user owner permissions,adminId:{},yzAccount:{},need permission:{},owner permisssions:{}", adminId,
                        SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT),permissionsEnum,userPermissions);
                return false;
            }else {
                Long needPermission = permissionsEnum.getPermissionsIndex().getValue();
               if( (userPermissions[permissionsEnum.getPermissionsIndex().getIndex()] & needPermission) != needPermission ) {
                   LOGGER.warn("interface permissons out of user owner permissions,adminId:{},yzAccount:{},need permission:{},owner permisssions:{}", adminId,
                           SessionTools.getInstance().get(SessionTools.YZ_ACCOUNT),permissionsEnum,Long.toHexString(needPermission));
               }else{
                   return true;
               }

            }
        }
        return false;
    }

}
