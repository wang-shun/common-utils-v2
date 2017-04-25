package com.youzan.sz.common.push.msg;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * Created by zhanguo on 2016/10/10.
 */
public class MsgDTO implements Serializable{
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
    /**
     * 接收时间
     * */
    private Long                recvDate;
    
    /**
     * 返回给前端的跳转url
     */
    private String routerUrl;
    
    /**
     * 消息类型图标
     */
    private String iconUrl;

    public Long getRecvDate() {
        return recvDate;
    }

    public void setRecvDate(Long recvDate) {
        this.recvDate = recvDate;
    }

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
    
    
    public String getRouterUrl() {
        return routerUrl;
    }
    
    
    public void setRouterUrl(String routerUrl) {
        this.routerUrl = routerUrl;
    }
    
    
    public String getIconUrl() {
        return iconUrl;
    }
    
    
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
