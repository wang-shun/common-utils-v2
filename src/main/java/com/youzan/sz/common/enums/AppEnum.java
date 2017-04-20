package com.youzan.sz.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by zhanguo on 16/8/18.
 */
public enum AppEnum {
                     FC(1, "fc", "免费收银"), //freeCheckout
                     SS(2, "ss", "有赞零售"), //super store
                     FSS(3, "fss", "有赞零售免费版"), //free super store
                     PC(11, "fss", "收银专业版"), //Professional Checkout
                     BEAUTY(21, "beauty", "美业"),
    ;
    private Integer                            aid;
    private String                             shortName;
    private String                             name;
    private final static Map<Integer, AppEnum> AID_MAPS = new HashMap<>();
    static {
        for (AppEnum appEnum : AppEnum.values()) {
            AID_MAPS.put(appEnum.aid, appEnum);
        }
    }

    AppEnum(Integer aid, String shortName, String name) {
        this.aid = aid;
        this.shortName = shortName;
        this.name = name;
    }

    public static AppEnum getAppByAid(Integer aId) {
        return AID_MAPS.get(aId);
    }

    public String getName() {
        return name;
    }

    public Integer getAid() {
        return aid;
    }

    public String getShortName() {
        return shortName;
    }
}
