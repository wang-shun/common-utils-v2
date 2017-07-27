package com.youzan.sz.common.extra;

import java.io.Serializable;

import lombok.Data;


/**
 * Created by jinxiaofei.
 * Time 2017/7/27 下午12:11
 * Desc 文件描述
 * 目前的二维码仅仅支持垂直居中的显示
 */
@Data
public class QrConfig implements Serializable{
    //二维码的大小
    private static final Integer QR_SIZE=200;
    //图片的大小
    private static final Integer LOGO_SIZE=55;

    private Integer qrSize=QR_SIZE;

    private Integer logoSize=LOGO_SIZE;

}
