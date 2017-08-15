package com.youzan.sz.common.extra;

import java.io.Serializable;

import lombok.Data;


/**
 * Created by jinxiaofei on 16/10/8.
 */
@Data
public class ImageConfig implements Serializable{
    //二维码大小
    private Integer qrcodeSize=200;
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

}
