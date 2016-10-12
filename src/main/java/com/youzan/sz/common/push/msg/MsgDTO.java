package com.youzan.sz.common.push.msg;

import java.util.Map;

/**
 *
 * Created by zhanguo on 2016/10/10.
 */
public class MsgDTO {
    /**
     * 可以用来避免重复发送
     * */
    private Long                id;
    private Long                bid;
    private Long                adminId;
    private Long                shopId;
    private String              title;
    private String              content;
    /**
     * 消息read(1->已读),其它未读
     * */
    private Integer             read;

    /**
     * 参数
     * */
    private Map<String, String> params;
    /**
     * 消息类型
     * */
    private int                 msgTypeEnum;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRead() {
        return read;
    }

    public void setRead(Integer read) {
        this.read = read;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public int getMsgTypeEnum() {
        return msgTypeEnum;
    }

    public void setMsgTypeEnum(int msgTypeEnum) {
        this.msgTypeEnum = msgTypeEnum;
    }
}