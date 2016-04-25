package com.youzan.sz.common.util;

import java.util.Date;

/**
 * Created by zefa on 16/4/9.
 */
public class StockRunningVO {
    private String businessType;//业务类型
    private String sourceOrderNo;//源单号
    private String productName;//商品名称
    private Date createTime;//创建时间
    private String createTimeDesc;//创建时间描述
    private String operatorStaffName;//经办人名称
    private String unit;//单位
    private int quantity;//数量

    public String toString(){
        String str =  "StockRunningVO{" +
                "businessType='" + businessType + '\'' +
                ", sourceOrderNo='" + sourceOrderNo + '\'' +
                ", productName='" + productName + '\'' +
                ", createTime=" + createTime +
                ", createTimeDesc='" + createTimeDesc + '\'' +
                ", operatorStaffName='" + operatorStaffName + '\'' +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                "}";
        return str;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getSourceOrderNo() {
        return sourceOrderNo;
    }

    public void setSourceOrderNo(String sourceOrderNo) {
        this.sourceOrderNo = sourceOrderNo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCreateTimeDesc() {
        return createTimeDesc;
    }

    public void setCreateTimeDesc(String createTimeDesc) {
        this.createTimeDesc = createTimeDesc;
    }

    public String getOperatorStaffName() {
        return operatorStaffName;
    }

    public void setOperatorStaffName(String operatorStaffName) {
        this.operatorStaffName = operatorStaffName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
