package com.youzan.sz.common.extra;

/**
 * Created by jinxiaofei on 16/10/8.
 */
public class ImageConfig {
    //二维码大小
    private int qrcodeSize;
    //镶嵌的横坐标
    private int XCoord;
    //镶嵌的纵坐标
    private int YCoord;
    //镶嵌的宽度
    private int width;
    //镶嵌的高度
    private int height;

    public ImageConfig(int qrcodeSize, int XCoord, int YCoord, int width, int height) {
        this.qrcodeSize = qrcodeSize;
        this.XCoord = XCoord;
        this.YCoord = YCoord;
        this.width = width;
        this.height = height;
    }

    public int getQrcodeSize() {
        return qrcodeSize;
    }

    public void setQrcodeSize(int qrcodeSize) {
        this.qrcodeSize = qrcodeSize;
    }

    public int getXCoord() {
        return XCoord;
    }

    public void setXCoord(int XCoord) {
        this.XCoord = XCoord;
    }

    public int getYCoord() {
        return YCoord;
    }

    public void setYCoord(int YCoord) {
        this.YCoord = YCoord;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
