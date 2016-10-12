package com.youzan.sz.common.push;

import com.youzan.sz.common.model.EnumValue;

/**
 *
 * Created by zhanguo on 2016/10/9.
 * 发送类型
 */
public enum SendType implements EnumValue {
                                           ADMIN_ID(1, "用户id"), //按照用户id发送
                                           ROLE(2, "角色id"), //按角色发送
                                           SHOP(3, "店铺id"), //按照店铺发送 
                                           SHOP_ROLE(4, "店铺id和角色id"), //店铺的某角色 
                                           DEVICE(5, "设备id");//按照设备发送
    private int    value;
    private String desc;

    SendType(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public int getValue() {
        return this.value;
    }

    @Override
    public String getName() {
        return this.desc;
    }

}
