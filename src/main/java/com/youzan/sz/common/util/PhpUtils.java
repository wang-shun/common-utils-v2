package com.youzan.sz.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.youzan.sz.common.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/2.
 * 处理调用php相关业务
 */
public class PhpUtils {
    private final static Boolean USE_HTTPS = false;
    private final static Logger  LOGGER    = LoggerFactory.getLogger(PhpUtils.class);

    /**
     * 直接返回通过json解析数据为bean
     * @param url 请求url
     *            @param params get请求的heade参数,不需要encoding
     * 
     * @throws com.youzan.platform.bootstrap.exception.BusinessException 连接错误,解码错误
     * */
    public static Result getResult(String url, Map<String, String> params) {
        String resp = get(url, params);
        if (resp == null) {
            return null;
        }
        try {
            return JsonUtils.json2Bean(resp, Result.class);
        } catch (Exception e) {
            LOGGER.error("get url({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }

    /**
     * 
     * */
    //    public static <T> T getJsonResponse(String url, Map<String, String> params, Class<T> responseClass) {
    //        Result result = getResult(url, params);
    //        if (result == null) {
    //            return null;
    //        }
    //        if(!result.isSucc()){
    //            ResponseCode.getMessageByCode()
    //        }
    //
    //
    //    }

    public static String get(String url, Map<String, String> params) {
        String response = null;
        try {
            response = HttpUtil.get(USE_HTTPS, params, url, StandardCharsets.UTF_8.displayName());
        } catch (IOException e) {
            LOGGER.error("php connect url({}) exception", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
        if (response == null) {//正常条件get请求不应该返回空值
            LOGGER.warn("get url({}) repsonse null", url);
        }

        return response;
    }

}
