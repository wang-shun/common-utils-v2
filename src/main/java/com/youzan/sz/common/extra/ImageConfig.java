package com.youzan.sz.common.extra;

/**
 * Created by jinxiaofei on 16/10/8.
 */
public class ImageConfig {
    //二维码大小
    private Integer qrcodeSize;
    //镶嵌的横坐标
    private Integer XCoord;
    //镶嵌的纵坐标
    private Integer YCoord;
    //镶嵌的宽度
    private Integer width;
    //镶嵌的高度
    private Integer height;

    //logo镶嵌的时候的大小,因为logo原图和镶嵌的时候可能不一样大;
    private Integer logoSize;
    //logo的横坐标
    private Integer logoXcoord;
    //logo的纵坐标
    private Integer logoYcoord;

    public Integer getQrcodeSize() {
        return qrcodeSize;
    }

    public void setQrcodeSize(Integer qrcodeSize) {
        this.qrcodeSize = qrcodeSize;
    }

    public Integer getXCoord() {
        return XCoord;
    }

    public void setXCoord(Integer XCoord) {
        this.XCoord = XCoord;
    }

    public Integer getYCoord() {
        return YCoord;
    }

    public void setYCoord(Integer YCoord) {
        this.YCoord = YCoord;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getLogoSize() {
        return logoSize;
    }

    public void setLogoSize(Integer logoSize) {
        this.logoSize = logoSize;
    }

    public Integer getLogoXcoord() {
        return logoXcoord;
    }

    public void setLogoXcoord(Integer logoXcoord) {
        this.logoXcoord = logoXcoord;
    }

    public Integer getLogoYcoord() {
        return logoYcoord;
    }

    public void setLogoYcoord(Integer logoYcoord) {
        this.logoYcoord = logoYcoord;
    }

    @Override
    public String toString() {
        return "ImageConfig{" +
                "qrcodeSize=" + qrcodeSize +
                ", XCoord=" + XCoord +
                ", YCoord=" + YCoord +
                ", width=" + width +
                ", height=" + height +
                ", logoSize=" + logoSize +
                ", logoXcoord=" + logoXcoord +
                ", logoYcoord=" + logoYcoord +
                '}';
    }
}
