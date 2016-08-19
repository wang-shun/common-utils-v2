package com.youzan.sz.common.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by zhanguo on 16/8/18.
 */
public enum AppEnum {
                     FC(1L, "fc", "免费收银"), //freeCheckout
                     SS(2L, "ss", "超级门店"), //super store
                     FSS(3L, "fss", "超级门店免费版"); //free super store
    private String                    desc;
    private Long                      aid;
    private String                    shortName;
    private static Map<Long, AppEnum> AID_MAPS = new HashMap<>();
    static {
        for (AppEnum AppEnum : AID_MAPS.values()) {
            AID_MAPS.put(AppEnum.aid, AppEnum);
        }
    }

    AppEnum(String desc, Long aid, String shortName) {
        this.desc = desc;
        this.aid = aid;
        this.shortName = shortName;
    }

    public static AppEnum getAppByAid(String aId) {
        return AID_MAPS.get(aId);
    }

    public String getDesc() {
        return desc;
    }

    public Long getAid() {
        return aid;
    }

    public String getShortName() {
        return shortName;
    }
}
