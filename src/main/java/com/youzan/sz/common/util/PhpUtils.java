package com.youzan.sz.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.sz.common.response.BaseResponse;
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
    public static <T> BaseResponse<T> getResult(String url, Map<String, String> params, Class<T> targetClass) {
        String resp = get(url, params);
        if (resp == null) {
            return null;
        }
        try {
            if (resp.contains("\"msg\":")) {//返回结构不一样,需要转换一次
                final HashMap<String, String> hashMap = JsonUtils.json2Bean(resp, HashMap.class);
                final String msg = hashMap.remove("msg");
                hashMap.put("message", msg);
                resp = JsonUtils.bean2Json(hashMap);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //            Result<T> result = objectMapper.readValue(resp, new TypeReference<Result<T>>() {
            //            });

            JavaType type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, targetClass);
            BaseResponse<T> result = objectMapper.readValue(resp, type);
            return result;
            //            Result<UserDetailDto> result = new ObjectMapper().readValue(src, new TypeReference<Result<UserDetailDto>>() {
            //            });
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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("call php url({}) resp({})", url, response);
        }

        return response;
    }

}