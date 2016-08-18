package com.youzan.sz.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by zhanguo on 16/8/18.
 */
public enum AppEnum {
                     SS("ss", "超级门店"), //super store
                     FSS("fss", "超级门店免费版"), //free super store
                     FC("fc", "免费收银");//freeCheckout
    private String                      desc;
    private String                      aid;
    private static Map<String, AppEnum> AID_MAPS = new HashMap<>();
    static {
        for (AppEnum AppEnum : AID_MAPS.values()) {
            AID_MAPS.put(AppEnum.aid, AppEnum);
        }
    }

    AppEnum(String desc, String aid) {
        this.desc = desc;
        this.aid = aid;
    }

    public static AppEnum getAppByAid(String aId) {
        return AID_MAPS.get(aId);
    }

    public String getDesc() {
        return desc;
    }

    public String getAid() {
        return aid;
    }

}
