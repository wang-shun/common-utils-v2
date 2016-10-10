package com.youzan.sz.common.extra;

import com.youzan.sz.common.model.qr.QRConfigVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by jinxiaofei on 16/10/8.
 * 为二维码添加水印
 */
public class WaterMarkUtil {
    private final static Logger LOGGER    = LoggerFactory.getLogger(WaterMarkUtil.class);


    // 二维码尺寸
    private static final int QRCODE_SIZE = 800;
    // 二维码的镶嵌宽度
    private static final int WIDTH = 940;
    private static final int HEIGHT = 940;

    //模板的x起始地方
    private static final int XCoord=280;
    //模板的y起始地方
    private static final int YCoord=700;

    public static BufferedImage createImage(String content, BufferedImage templateImg) throws IOException {
        QRConfigVO qrConfigVO=new QRConfigVO();
        qrConfigVO.setTxt(content);
        qrConfigVO.setSize(QRCODE_SIZE);
        String QRuRL=QRUtils.getQRCode(qrConfigVO);


        BufferedImage source= null;
        try {
            source = ImageIO.read(new URL(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}",e);
            throw e;
        }
        int width = templateImg.getWidth(null);
        int height = templateImg.getHeight(null);
        BufferedImage tag = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(templateImg,0,0,null);
        // 插入LOGO
        g.drawImage(source, XCoord, YCoord, WIDTH, HEIGHT, null);
        g.dispose();
        // 插入图片
        return tag;
    }

    /**
     *
     * @param content 内容
     * @param templateImg 模板水印的图像
     * @param imageConfig 设置项,图片的位置
     * @return
     * @throws IOException
     */
    public static BufferedImage createImage(String content, BufferedImage templateImg,ImageConfig imageConfig) throws IOException {
        QRConfigVO qrConfigVO=new QRConfigVO();
        qrConfigVO.setTxt(content);
        qrConfigVO.setSize(imageConfig.getQrcodeSize());
        String QRuRL=QRUtils.getQRCode(qrConfigVO);


        BufferedImage source= null;
        try {
            source = ImageIO.read(new File(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}",e);
            throw e;
        }
        int width = templateImg.getWidth(null);
        int height = templateImg.getHeight(null);
        BufferedImage tag = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(templateImg,0,0,null);
        g.drawImage(source, imageConfig.getXCoord(), imageConfig.getYCoord(), imageConfig.getWidth(), imageConfig.getHeight(), null);
        g.dispose();
        // 插入图片
        return tag;
    }

    public static void main(String[] args) throws IOException {
        BufferedImage template=null;
        String text = "http://www.baidu.com";
        try {
             template=ImageIO.read(new File("/Users/jinxiaofei/qrtest/template.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage result=WaterMarkUtil.createImage(text,template);
        ImageIO.write( result,"jpg", new File("/Users/jinxiaofei/qrtest/qrcode.jpg"));

    }
}