package com.youzan.sz.common.extra;

import com.youzan.sz.common.model.qr.QRConfigVO;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.jutil.string.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;


/**
 * Created by jinxiaofei on 16/10/8.
 * 为二维码添加水印
 */
public class WaterMarkUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(WaterMarkUtil.class);


    // 二维码尺寸
    private static final int QRCODE_SIZE = 800;

    // 二维码的镶嵌宽度
    private static final int WIDTH = 900;

    private static final int HEIGHT = 900;


    //模板的x起始地方
    private static final int XCoord = 300;

    //模板的y起始地方
    private static final int YCoord = 870;

    private static final int LOGO_XCOORD = 660;

    private static final int LOGO_YCOORD = 1230;

    private static final int LOGO_SIZE = 180;

    private static final ImageConfig DEFAULT_IMAGE_CONFIG = WaterMarkUtil.getDefaultConfig();


    /**
     * @param content 二维码的内容
     * @param templateImg 模板的图片
     * @param logoImg logo的图片
     * @param imageConfig 图片镶嵌的私人化的设置
     */
    public static BufferedImage createImage(String content, BufferedImage templateImg, BufferedImage logoImg, ImageConfig imageConfig) throws IOException {
        imageConfig = inherrateConifg(imageConfig);
        QRConfigVO qrConfigVO = new QRConfigVO();
        qrConfigVO.setTxt(content);
        qrConfigVO.setSize(imageConfig.getQrcodeSize());
        if (logoImg != null) {
            qrConfigVO.setLevel(1);
        }
        String QRuRL = QRUtils.getQRCode(qrConfigVO);


        BufferedImage source = null;
        try {
            source = ImageIO.read(new URL(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}", e);
            throw e;
        }
        int width = templateImg.getWidth(null);
        int height = templateImg.getHeight(null);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(templateImg, 0, 0, null);
        // 插入二维码
        g.drawImage(source, imageConfig.getXCoord(), imageConfig.getYCoord(), imageConfig.getWidth(), imageConfig.getHeight(), null);
        // 插入logo,对logo进行压缩,不然太大

        if (logoImg != null) {
            Image logoScale = logoImg.getScaledInstance(imageConfig.getLogoSize(), imageConfig.getLogoSize(), Image.SCALE_SMOOTH);
            g.drawImage(logoScale, imageConfig.getLogoXcoord(), imageConfig.getLogoYcoord(), imageConfig.getLogoSize(), imageConfig.getLogoSize(), null);
            logoScale.flush();
        }
        //释放图片
        g.dispose();
        if (source != null) {
            source.flush();
        }
        // 插入图片
        return tag;
    }


    /**
     * @param content 二维码的内容
     * @param templateImg 模板的图片
     * @param logoImg logo的图片
     * @param imageConfig 图片镶嵌的私人化的设置
     * @return 圆形的logo
     */
    public static BufferedImage createImageWithCircleLogo(String content, BufferedImage templateImg, BufferedImage logoImg, ImageConfig imageConfig) throws IOException {
        imageConfig = inherrateConifg(imageConfig);
        QRConfigVO qrConfigVO = new QRConfigVO();
        qrConfigVO.setTxt(content);
        qrConfigVO.setSize(imageConfig.getQrcodeSize());
        if (logoImg != null) {
            qrConfigVO.setLevel(2);
        }
        String QRuRL = QRUtils.getQRCode(qrConfigVO);
        BufferedImage source = null;
        try {
            source = ImageIO.read(new URL(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}", e);
            throw e;
        }
        int width = templateImg.getWidth(null);
        int height = templateImg.getHeight(null);
        BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(templateImg, 0, 0, null);
        // 插入二维码
        g.drawImage(source, (width - imageConfig.getQrcodeSize()) / 2, imageConfig.getYCoord(), imageConfig.getQrcodeSize(), imageConfig.getQrcodeSize(), null);
        // 插入logo,对logo进行压缩,不然太大

        if (logoImg != null) {
            Image logoScale = logoImg.getScaledInstance(imageConfig.getLogoSize(), imageConfig.getLogoSize(), Image.SCALE_SMOOTH);
            g.drawImage(logoScale, (width - imageConfig.getLogoSize()) / 2, imageConfig.getYCoord() + (imageConfig.getQrcodeSize() - imageConfig.getLogoSize()) / 2, imageConfig.getLogoSize(),
                    imageConfig.getLogoSize(), null);
            logoScale.flush();
        }
        //释放图片
        g.dispose();
        if (source != null) {
            source.flush();
        }
        // 插入图片
        return tag;
    }


    /**
     * 没有模板的情况下
     *
     * @param content 二维码的内容
     * @param logoImg logo的图片
     * @param imageConfig 图片镶嵌的私人化的设置
     */
    public static BufferedImage createImage(String content, BufferedImage logoImg, ImageConfig imageConfig) throws IOException {
        imageConfig = inherrateConifg(imageConfig);
        QRConfigVO qrConfigVO = new QRConfigVO();
        qrConfigVO.setTxt(content);
        qrConfigVO.setSize(imageConfig.getQrcodeSize());
        if (logoImg != null) {
            qrConfigVO.setLevel(2);
        }
        String QRuRL = QRUtils.getQRCode(qrConfigVO);


        BufferedImage source = null;
        try {
            source = ImageIO.read(new URL(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}", e);
            throw e;
        }
        BufferedImage tag = new BufferedImage(imageConfig.getQrcodeSize(), imageConfig.getQrcodeSize(), BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        // 插入二维码
        g.drawImage(source, 0, 0, imageConfig.getQrcodeSize(), imageConfig.getQrcodeSize(), null);
        // 插入logo,对logo进行压缩,不然太大

        if (logoImg != null) {
            Image logoScale = logoImg.getScaledInstance(imageConfig.getLogoSize(), imageConfig.getLogoSize(), Image.SCALE_SMOOTH);
            g.drawImage(logoScale, (imageConfig.getQrcodeSize() - imageConfig.getLogoSize()) / 2, (imageConfig.getQrcodeSize() - imageConfig.getLogoSize()) / 2, imageConfig.getLogoSize(),
                    imageConfig.getLogoSize(), null);
            logoScale.flush();
        }
        //释放图片
        g.dispose();
        if (source != null) {
            source.flush();
        }
        // 插入图片
        return tag;
    }


    /**
     * 创建带有logo的base64图片
     *
     * @param content 二维码的内容
     * @param logoUrl logo的图片的地址
     */
    public static String createQRWithImg(String content, String logoUrl, QrConfig qrConfig) throws IOException {
        QrConfig tempQrConfig=qrConfig;
        if (tempQrConfig == null) {
            tempQrConfig = new QrConfig();
        }

        QRConfigVO qrConfigVO = new QRConfigVO();
        qrConfigVO.setTxt(content);
        //如果图片url不为空，那么二维码的冗余度要大一些
        if (StringUtil.isNotEmpty(logoUrl)) {
            qrConfigVO.setLevel(3);
        }
        String QRuRL = QRUtils.getQRCode(qrConfigVO);

        BufferedImage source ;
        BufferedImage logo = null;
        try {
            source = ImageIO.read(new URL(QRuRL));
        } catch (IOException e) {
            LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}", e);
            return null;
        }
        if(StringUtil.isNotEmpty(logoUrl)){
            try {
                logo = ImageIO.read(new URL(logoUrl));
            } catch (IOException e) {
                LOGGER.warn("二维码服务不可用,无法获取到为二维码:{}", e);
            }
        }

        BufferedImage tag = new BufferedImage(tempQrConfig.getQrSize(), tempQrConfig.getQrSize(), BufferedImage.TYPE_INT_RGB);
        Graphics g = tag.getGraphics();
        g.drawImage(source, 0, 0, tempQrConfig.getQrSize(), tempQrConfig.getQrSize(), null);
        // 插入二维码
        if (logo != null) {
            // 插入logo,对logo进行压缩,不然太大
            Image logoScale = logo.getScaledInstance(tempQrConfig.getLogoSize(), tempQrConfig.getLogoSize(), Image.SCALE_SMOOTH);
            g.drawImage(logoScale, (tempQrConfig.getQrSize() - tempQrConfig.getLogoSize()) / 2, (tempQrConfig.getQrSize() - tempQrConfig.getLogoSize()) / 2, tempQrConfig.getLogoSize(),
                    tempQrConfig.getLogoSize(), null);
            logoScale.flush();
        }
        //释放图片
        g.dispose();
        if (source != null) {
            source.flush();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(tag, "png", bos);
        byte[] imageBytes = bos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }


    public static String getBase64logoImage(String content, BufferedImage logoImg, ImageConfig imageConfig) throws IOException {
        BufferedImage targetImage = createImage(content, logoImg, imageConfig);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(targetImage, "png", bos);
            byte[] imageBytes = bos.toByteArray();
            targetImage.flush();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            LOGGER.error("encode to base64 error,qrConfig:{}", JsonUtils.bean2Json(imageConfig), e);
            throw e;
        }
    }


    private static ImageConfig inherrateConifg(ImageConfig imageConfig) {
        if (imageConfig == null) {
            return DEFAULT_IMAGE_CONFIG;
        }
        if (imageConfig.getHeight() == null || imageConfig.getHeight() == 0) {
            imageConfig.setHeight(DEFAULT_IMAGE_CONFIG.getHeight());
        }
        if (imageConfig.getWidth() == null || imageConfig.getWidth() == 0) {
            imageConfig.setWidth(DEFAULT_IMAGE_CONFIG.getWidth());
        }
        if (imageConfig.getXCoord() == null || imageConfig.getXCoord() == 0) {
            imageConfig.setXCoord(DEFAULT_IMAGE_CONFIG.getXCoord());
        }
        if (imageConfig.getYCoord() == null || imageConfig.getYCoord() == 0) {
            imageConfig.setYCoord(DEFAULT_IMAGE_CONFIG.getYCoord());
        }
        if (imageConfig.getQrcodeSize() == null || imageConfig.getQrcodeSize() == 0) {
            imageConfig.setQrcodeSize(DEFAULT_IMAGE_CONFIG.getQrcodeSize());
        }


        if (imageConfig.getLogoSize() == null || imageConfig.getLogoSize() == 0) {
            imageConfig.setLogoSize(DEFAULT_IMAGE_CONFIG.getLogoSize());
        }
        if (imageConfig.getLogoXcoord() == null || imageConfig.getLogoXcoord() == 0) {
            imageConfig.setLogoXcoord(DEFAULT_IMAGE_CONFIG.getLogoXcoord());
        }
        if (imageConfig.getYCoord() == null || imageConfig.getYCoord() == 0) {
            imageConfig.setYCoord(DEFAULT_IMAGE_CONFIG.getYCoord());
        }

        return imageConfig;
    }


    private static ImageConfig getDefaultConfig() {
        final ImageConfig imageConfig = new ImageConfig();
        imageConfig.setQrcodeSize(QRCODE_SIZE);
        imageConfig.setHeight(HEIGHT);
        imageConfig.setWidth(WIDTH);

        imageConfig.setXCoord(XCoord);
        imageConfig.setYCoord(YCoord);

        imageConfig.setLogoSize(LOGO_SIZE);
        imageConfig.setLogoXcoord(LOGO_XCOORD);
        imageConfig.setLogoYcoord(LOGO_YCOORD);
        return imageConfig;
    }


    public static void main(String[] args) throws IOException {
        BufferedImage template = null;
        BufferedImage templateCard = null;
        BufferedImage badgeTemplate = null;
        BufferedImage logo = null;
        String text = "http://www.youzan.com?kw=aaaaaaaaa";
        /*try {
             template=ImageIO.read(new File("/Users/jinxiaofei/qrtest/template2.png"));
            logo=ImageIO.read(new File("/Users/jinxiaofei/qrtest/logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage result=WaterMarkUtil.createImage(text,logo,null);
        ImageConfig imageConfig=new ImageConfig();
        imageConfig.setQrcodeSize(400);
        imageConfig.setLogoSize(100);
        System.out.println(WaterMarkUtil.getBase64logoImage(text,logo,imageConfig));*/
        try {
            template = ImageIO.read(new File("/Users/jinxiaofei/qrtest/stick-template.png"));
            templateCard = ImageIO.read(new File("/Users/jinxiaofei/qrtest/card-template.png"));
            badgeTemplate = ImageIO.read(new File("/Users/jinxiaofei/qrtest/badge-template.png"));
            logo = ImageIO.read(new File("/Users/jinxiaofei/qrtest/circle-logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //不干胶
        ImageConfig imageConfig = new ImageConfig();
        imageConfig.setQrcodeSize(568);
        imageConfig.setYCoord(680);
        imageConfig.setLogoSize(100);
        BufferedImage result = WaterMarkUtil.createImageWithCircleLogo(text, template, logo, imageConfig);
        ImageIO.write(result, "png", new File("/Users/jinxiaofei/qrtest/stick.png"));

        //台卡
        ImageConfig imageConfig2 = new ImageConfig();
        imageConfig2.setQrcodeSize(886);
        imageConfig2.setYCoord(910);
        imageConfig2.setLogoSize(150);
        BufferedImage result2 = WaterMarkUtil.createImageWithCircleLogo(text, templateCard, logo, imageConfig2);
        ImageIO.write(result2, "png", new File("/Users/jinxiaofei/qrtest/card.png"));

        //胸牌
        ImageConfig imageConfig3 = new ImageConfig();
        imageConfig3.setQrcodeSize(422);
        imageConfig3.setYCoord(471);
        imageConfig3.setLogoSize(80);
        BufferedImage result3 = WaterMarkUtil.createImageWithCircleLogo(text, badgeTemplate, logo, imageConfig3);
        ImageIO.write(result3, "png", new File("/Users/jinxiaofei/qrtest/badge.png"));

    }
}