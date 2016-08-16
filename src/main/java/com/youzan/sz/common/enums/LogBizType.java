package com.youzan.sz.common.enums;

/**
 *
 * Created by zhanguo on 16/8/16.
 */
public enum LogBizType {
                        OA_HANDOVER_TOWORK("搅拌后开始上班");

    LogBizType(String desc) {
        this.desc = desc;
    }

    private String biz;
    private String desc;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
