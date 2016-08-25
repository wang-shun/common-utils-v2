package com.youzan.sz.common.extra;

import com.alibaba.fastjson.JSONObject;
import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PhpUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 *
 * Created by zhanguo on 16/8/24.
 * 短地址服务
 */
public class ShortUrlUtils {
    private final static Logger LOGGER  = LoggerFactory.getLogger(ShortUrlUtils.class);
    private final static String API_URL = "yz.short.url";

    /**
     * api是http://api.kdt.im/shorten?longUrl=$url
     * 目前只有koudaitong.com, youzan.com, qima-inc.com这几个域名下的可以服务 。
     *@param longUrl 未encodingurl
     * */
    public static String shortRawUrl(String longUrl) {
        String encodingUrl;
        try {
            encodingUrl = URLEncoder.encode(longUrl, StandardCharsets.UTF_8.name());
            return shortUrl(encodingUrl);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("url encoding error", e);
            return longUrl;
        }
    }

    public static String shortUrl(String longUrl) {
        final String apiUrl = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, API_URL) + longUrl;
        final String resp = PhpUtils.get(apiUrl, Collections.emptyMap());
        final JSONObject jsonObject = JSONObject.parseObject(resp, JSONObject.class);
        if (jsonObject == null) {
            LOGGER.error("短链接生成异常,没有返回data,resp:{},原链接:{}", resp, longUrl);
            return StringUtil.EMPTY;
        }
        final JSONObject respData = (JSONObject) jsonObject.get("data");
        if (respData == null) {
            LOGGER.error("短链接生成异常,没有返回data,resp:{},原链接:{}", jsonObject, longUrl);
            return StringUtil.EMPTY;
        }
        final String url = (String) respData.get("url");
        if (StringUtil.isEmpty(url)) {
            LOGGER.error("短链接生成异常,没有返回data,resp:{},原链接:{}", respData, longUrl);
            return StringUtil.EMPTY;
        }
        return url;
    }

    public static void main(String[] args) {
        String resp = "{\"status_txt\":\"ok\",\"data\":{\"url\":\"http:\\/\\/shorturl-qa.s.qima-inc.com\\/HDDK1r\",\"hash\":\"HDDK1r\",\"long_url\":\"http:\\/\\/my.test.youzan.com\\/\",\"global_hash\":\"HDDK1r\"},\"status_code\":200}";

        final JSONObject jsonObject = JSONObject.parseObject(resp, JSONObject.class);
        final JSONObject respData = (JSONObject) jsonObject.get("data");
        if (respData == null) {
            LOGGER.error("短链接生成异常,没有返回data,resp:{}", resp);
        }
        final String url = (String) respData.get("url");
        if (StringUtil.isEmpty(url)) {
            LOGGER.error("短链接生成异常,没有返回data,resp:{}", respData);
        }
        System.err.println(url);
    }
}
