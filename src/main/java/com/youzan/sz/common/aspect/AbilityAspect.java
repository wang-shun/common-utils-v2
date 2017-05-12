package com.youzan.sz.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.annotation.Ability;
import com.youzan.sz.common.enums.AbilityEnum;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.NumberUtils;
import com.youzan.sz.common.util.PhpUtils;
import com.youzan.sz.jutil.string.StringUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by jinxiaofei on 2016/12/5.
 * 用于对店铺的English的检验比如说微商城的提现店铺能力等的校验
 */
@Aspect
public class AbilityAspect extends BaseAspect {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilityAspect.class);
    
    private static final String SHOP_INFO_URL = "http://api.koudaitong.com/shop/shop/Shop/getShopByGroupId";
    
    private static final String SHOP_ABILITY_URL = "http://api.koudaitong.com/account/teamStatus/getStatus";
    
    
    @Pointcut("@annotation(com.youzan.sz.common.annotation.Ability)")
    public void pointcut() {
        
    }
    
    
    @Before("pointcut()")
    //在方法执行前检查提现的能力
    public void before(JoinPoint joinPoint) {
        
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Ability ability = methodSignature.getMethod().getAnnotation(Ability.class);
        long kdtId;
        AbilityEnum abilityEnum = ability.biz();
        if (NumberUtils.isPositive(ability.kdtId())) {
            kdtId = ability.kdtId();
        }else {
            kdtId = DistributedContextTools.getKdtId();
        }
        LOGGER.info("begin to check shop ability ,kdtId:{},ability:{}", kdtId, abilityEnum.getName());
        boolean isAbilityAble = checkAbility(kdtId, abilityEnum);
        if(!isAbilityAble){
            LOGGER.info("ABILITY IS LOCK ,kdtId:{},ability:{}",kdtId,abilityEnum.getName());
            throw new BusinessException(abilityEnum.getErrorCode(), ResponseCode.getMessageByCode(abilityEnum.getErrorCode()));
        }
        
    }
    
    
    private boolean checkAbility(long kdtId, AbilityEnum abilityEnum) {
        
        Map<String, String> param = new HashMap<>();
        param.put("debug", "json");
        param.put("kdt_id", kdtId + "");
        param.put("kdtId", kdtId + "");
        String url = null;
        if (abilityEnum.getSource() == 1) {
            url = SHOP_ABILITY_URL;
        }else if (abilityEnum.getSource() == 2) {
            url = SHOP_INFO_URL;
        }
        String result = getHttpResult(url, param);
        if (StringUtil.isEmpty(result)) {
            return true;
        }else {
            JSONObject jsonObject = JSON.parseObject(result);
            if (jsonObject.getInteger("code") == 0) {
                JSONObject data = jsonObject.getJSONObject("data");
                if (data == null || data.isEmpty()) {
                    return true;
                }else {
                    int isOk = data.getInteger(abilityEnum.getProperty());
                    return isOk == 0;
                }
            }else {
                LOGGER.warn("the result of shop ability is wrong,just skip the ability check");
                return true;
            }
        }
    }
    
    
    private String getHttpResult(String url, Map<String, String> param) {
        
        String result;
        try {
            result = PhpUtils.post(url, param);
        } catch (Exception e) {
            LOGGER.warn("get shop ability from hz occur error", e);
            //
            return null;
        }
        LOGGER.info("get shop ability from hz,result:{}", result);
        return result;
    }
    
}
