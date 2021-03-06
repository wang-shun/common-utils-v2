package com.youzan.sz.common.extra;

import com.youzan.sz.common.model.qr.QRConfigVO;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import com.youzan.sz.jutil.string.StringUtil;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 *
 * Created by zhanguo on 16/8/22.
 * 使用公司公用二维码服务
 * 参数说明<a href="http://doc.qima-inc.com/pages/viewpage.action?pageId=4326255">参考</a>
 */

public class QRUtils {
    private final static Logger LOGGER    = LoggerFactory.getLogger(QRUtils.class);
    private final static String YZ_QR_URL = "yz.qr.url";

    //    单个接口:http://10.9.17.31:8888/?size=200&fg_color=000000&bg_color=ffffff&case=1&txt=12a&margin=0&level=3&hint=2&ver=2
    //    批量接口:http://10.9.17.31:8888/qrcode/batch?size=200&fg_color=000000&bg_color=ffffff&case=1&margin=0&level=3&hint=2&ver=2&txt[]=123&txt[]=456
    public static String getQRCode(QRConfigVO qrConfig) {
        //String yzQRUrl = "http://10.9.17.31:8888";
        final String yzQRUrl = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, YZ_QR_URL);
        final StringBuilder url = new StringBuilder(yzQRUrl);
        if (url.lastIndexOf("/") == url.indexOf("//") + 1) {//如果最后一个不是/,需要添加,避免某些情况Android无法访问
            url.append("/");
        }

        if (qrConfig.getTxts() == null) {
            url.append("?").append("txt=").append(qrConfig.getTxt());//
        } else {
            url.append("?").append("txt=").append(qrConfig.getTxts());//
        }
        url.append("&").append("size=").append(qrConfig.getSize());//
        url.append("&").append("fg_color=").append(qrConfig.getFgColor());//
        url.append("&").append("bg_color=").append(qrConfig.getBgColor());//
        url.append("&").append("case=").append(qrConfig.isIgnoreCase() ? 0 : 1);//
        url.append("&").append("margin=").append(qrConfig.getMargin());//
        url.append("&").append("level=").append(qrConfig.getLevel());//
        url.append("&").append("hint=").append(qrConfig.getHint());//
        url.append("&").append("ver=").append(qrConfig.getVer());//
        if(StringUtil.isNotEmpty(qrConfig.getCp())){
            url.append("&").append("cp=").append(qrConfig.getCp());//
        }
        return url.toString();
    }

    public static String getBase64QR(String qrUrl) {
        Base64 encoder = new Base64();
        byte[] imageBytes = new byte[0];
        try {
            URL url = new URL(qrUrl);
            final BufferedImage image = ImageIO.read(url);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", bos);
            imageBytes = bos.toByteArray();
        } catch (IOException e) {
            LOGGER.warn("encode to base64 error,qrConfig:{}", JsonUtils.bean2Json(qrUrl), e);
        }
        return encoder.encodeAsString(imageBytes);
    }

    public static void main(String[] args) throws IOException {
        String logo="http://img.yzcdn.cn/upload_files/2017/07/27/Flbf2KrMpN8WVIFc3O9YNTp25iru.jpg";
        final long start = System.currentTimeMillis();
       /*

        final URL imgUrl = new URL(logo);
        final BufferedImage image = ImageIO.read(imgUrl);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        byte[] imageBytes = bos.toByteArray();
        Base64 encoder = new Base64();

        final String encode = encoder.encodeAsString(imageBytes);
        System.err.println(encode);
        QRConfigVO qrConfigVO = new QRConfigVO();
        qrConfigVO.setCp(encode);
        qrConfigVO.setTxt("www.youzan.com");
        String qrUrl=getQRCode(qrConfigVO);
        System.out.println("the qr url is:"+qrUrl);
        System.err.println("len" + encode.length());*/
        //System.out.println(WaterMarkUtil.createQRWithImg("www.youzan.com",logo,null));
        System.out.println(WaterMarkUtil.getBase64QR(logo,50));
        System.out.println(System.currentTimeMillis()-start);

    }

}
