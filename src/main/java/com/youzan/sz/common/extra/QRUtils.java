package com.youzan.sz.common.extra;

import com.youzan.sz.common.model.qr.QRConfigVO;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

/**
 *
 * Created by zhanguo on 16/8/22.
 * 使用公司公用二维码服务
 * 参数说明<a href="http://doc.qima-inc.com/pages/viewpage.action?pageId=4326255">参考</a>
 */

public class QRUtils {
    private final static String YZ_QR_URL = "yz.qr.url";

    //    单个接口:http://10.9.17.31:8888/?size=200&fg_color=000000&bg_color=ffffff&case=1&txt=12a&margin=0&level=3&hint=2&ver=2
    //    批量接口:http://10.9.17.31:8888/qrcode/batch?size=200&fg_color=000000&bg_color=ffffff&case=1&margin=0&level=3&hint=2&ver=2&txt[]=123&txt[]=456
    public static String getQRCode(QRConfigVO qrConfig) {
        final String yzQRUrl = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, YZ_QR_URL);
        final StringBuilder url = new StringBuilder(yzQRUrl).append("/");
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
        return url.toString();
    }

}
