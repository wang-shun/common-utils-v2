package com.youzan.sz.common.nsq;

/**
 * Created by jinxiaofei on 16/7/21.
 * 确定一个共同的传递信息的消息体,自己可以继承
 */
public class NSQmsgBean {
    private Long bid;
    private Long id;
    private Long shopId;
    private Long staffId;
    private Object data;

    @Override
    public String toString() {
        return "NSQmsgBean{" +
                "bid=" + bid +
                ", id=" + id +
                ", shopId=" + shopId +
                ", staffId=" + staffId +
                ", data=" + data +
                '}';
    }

    public Long getBid() {
        return bid;
    }

    public void setBid(Long bid) {
        this.bid = bid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
