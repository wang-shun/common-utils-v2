package com.youzan.sz.common.extra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Created by jinxiaofei on 16/10/25.
 */
public class ImageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);

    /**
     * 翻转图片
     * @param bufferedimage 目标图片
     * @return
     */
    public static void flipImage(final BufferedImage bufferedimage) {
        if(bufferedimage==null){
            LOGGER.warn("目标图片为空,不进行操作");
            return;
        }
        AffineTransform transform = new AffineTransform();
        transform.scale(-1, 1);
        transform.translate(-2 * bufferedimage.getWidth(null) / 2,0);

        Graphics2D g2 = (Graphics2D) bufferedimage.getGraphics();
        g2.drawImage(bufferedimage,transform,null) ;
        g2.dispose();
        bufferedimage.flush();
    }

}
