package com.youzan.sz.common.enums;


import java.util.Arrays;
import java.util.Objects;


/**
 * Created by zhanguo on 16/8/18.
 */
public enum AbilityEnum {
    WITHDRAW(1, 1, "is_drawal_lock", "提现锁定", 14104),
    //freeCheckout
    TRADE(2, 1, "is_trade_lock", "交易锁定", 14103),
    //super store
    SHOP_LOCK(3, 2, "isLocked", "店铺锁定", 14108),
    //free super store
    ;
    
    private Integer biz;
    
    private Integer source;
    
    private String property;
    
    private String name;
    
    private Integer errorCode;
    
    
    AbilityEnum(Integer biz, Integer source, String property, String name, Integer errorCode) {
        
        this.biz = biz;
        this.source = source;
        this.property = property;
        this.name = name;
        this.errorCode = errorCode;
    }
    
    
    public static AbilityEnum getAbilityByBiz(Integer biz) {
        
        return Arrays.stream(AbilityEnum.values()).filter(inputBiz -> Objects.equals(inputBiz.getBiz(), biz)).findFirst().orElse(null);
    }
    
    
    public String getName() {
        
        return name;
    }
    
    
    public Integer getBiz() {
        
        return biz;
    }
    
    
    public String getProperty() {
        
        return property;
    }
    
    
    public Integer getSource() {
        
        return source;
    }
    
    
    public Integer getErrorCode() {
        
        return errorCode;
    }
}

