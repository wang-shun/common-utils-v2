package com.youzan.sz.common.push.msg;

import com.youzan.sz.common.model.PageDTO;

/**
 *
 * Created by zhanguo on 2016/10/10.
 */
public class MsgPageDTO extends PageDTO {
    /**
     * 可以用来避免重复发送
     * */
    private Long id;
    private Long bid;
    private Long adminId;
    private Long shopId;
    /**
     * 消息类型
     * */
    private int  msgTypeEnum;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBid() {
        return bid;
    }

    public void setBid(Long bid) {
        this.bid = bid;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public int getMsgTypeEnum() {
        return msgTypeEnum;
    }

    public void setMsgTypeEnum(int msgTypeEnum) {
        this.msgTypeEnum = msgTypeEnum;
    }
}
