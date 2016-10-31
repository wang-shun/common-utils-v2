package com.youzan.sz.common.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
                final HashMap hashMap = JsonUtils.json2Bean(resp, HashMap.class);
                Object data = hashMap.get("data");
                final Object msg = hashMap.getOrDefault("msg", "");

                if (new Integer(0).equals(hashMap.get("code"))) {//返回成功
                    if (data != null && (data instanceof Collection)) {//处理空集合返回
                        if (CollectionUtils.isEmpty((Collection) data)) {
                            LOGGER.debug("data没数据直接返回");
                            return new BaseResponse(Integer.valueOf(hashMap.get("code").toString()), msg.toString(),
                                null);
                        }
                    }
                }

                final Object oldMsg = hashMap.remove("msg");
                hashMap.put("message", oldMsg == null ? "" : oldMsg.toString());
                resp = JsonUtils.bean2Json(hashMap);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
     * 直接返回通过json解析数据为bean
     * @param url 请求url
     *            @param params get请求的heade参数,不需要encoding
     *
     * @throws com.youzan.platform.bootstrap.exception.BusinessException 连接错误,解码错误
     * */
    public static <T> BaseResponse getResultWithNamingStrategy(String url, Map<String, String> params,
                                                               Class<T> targetClass,

                                                               PropertyNamingStrategy s,
                                                               Map<String, String> jsonTransferFiled, Boolean isList) {
        String resp = get(url, params);
        if (resp == null) {
            return null;
        }
        try {
            return dealHttpResult(resp,targetClass,s,jsonTransferFiled,isList);
            /*if (resp.contains("\"msg\":")) {//返回结构不一样,需要转换一次
                final HashMap hashMap = JsonUtils.json2Bean(resp, HashMap.class);
                Object data = hashMap.get("data");
                final Object msg = hashMap.getOrDefault("msg", "");

                if (new Integer(0).equals(hashMap.get("code"))) {//返回成功
                    if (data != null && (data instanceof Collection)) {//处理空集合返回
                        if (CollectionUtils.isEmpty((Collection) data)) {
                            LOGGER.debug("data没数据直接返回");
                            return new BaseResponse(Integer.valueOf(hashMap.get("code").toString()), msg.toString(),
                                null);
                        }
                    }
                }

                final Object oldMsg = hashMap.remove("msg");
                hashMap.put("message", oldMsg == null ? "" : oldMsg.toString());
                resp = JsonUtils.bean2Json(hashMap);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            if (s != null) {
                objectMapper.setPropertyNamingStrategy(s);
            }
            if (jsonTransferFiled != null && jsonTransferFiled.size() != 0) {
                Iterator entries = jsonTransferFiled.entrySet().iterator();

                while (entries.hasNext()) {

                    Map.Entry entry = (Map.Entry) entries.next();

                    String key = (String) entry.getKey();

                    String value = (String) entry.getValue();
                    resp = resp.replaceAll(key, value);

                }
            }
            JavaType type = null;
            if (isList == true) {
                JavaType type1 = objectMapper.getTypeFactory().constructParametricType(List.class, targetClass);
                type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, type1);
            } else {
                type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, targetClass);
            }
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            BaseResponse<T> result = objectMapper.readValue(resp, type);
            return result;*/
        } catch (Exception e) {
            LOGGER.error("get url({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }

    private static <T> BaseResponse dealHttpResult(String resp, Class<T> targetClass,

                                                   PropertyNamingStrategy s, Map<String, String> jsonTransferFiled,
                                                   Boolean isList) throws  Exception {
        if (resp == null) {
            return null;
        }
        try {
            if (resp.contains("\"msg\":")) {//返回结构不一样,需要转换一次
                final HashMap hashMap = JsonUtils.json2Bean(resp, HashMap.class);
                Object data = hashMap.get("data");
                final Object msg = hashMap.getOrDefault("msg", "");

                if (new Integer(0).equals(hashMap.get("code"))) {//返回成功
                    if (data != null && (data instanceof Collection)) {//处理空集合返回
                        if (CollectionUtils.isEmpty((Collection) data)) {
                            LOGGER.debug("data没数据直接返回");
                            return new BaseResponse(Integer.valueOf(hashMap.get("code").toString()), msg.toString(),
                                null);
                        }
                    }
                }

                final Object oldMsg = hashMap.remove("msg");
                hashMap.put("message", oldMsg == null ? "" : oldMsg.toString());
                resp = JsonUtils.bean2Json(hashMap);
            }

            ObjectMapper objectMapper = new ObjectMapper();
            if (s != null) {
                objectMapper.setPropertyNamingStrategy(s);
            }
            if (jsonTransferFiled != null && jsonTransferFiled.size() != 0) {
                Iterator entries = jsonTransferFiled.entrySet().iterator();

                while (entries.hasNext()) {

                    Map.Entry entry = (Map.Entry) entries.next();

                    String key = (String) entry.getKey();

                    String value = (String) entry.getValue();
                    resp = resp.replaceAll(key, value);

                }
            }
            JavaType type = null;
            if (isList == true) {
                JavaType type1 = objectMapper.getTypeFactory().constructParametricType(List.class, targetClass);
                type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, type1);
            } else {
                type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, targetClass);
            }
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            BaseResponse<T> result = objectMapper.readValue(resp, type);
            return result;
        }  catch(Exception e) {
            LOGGER.error("deal with result ({}) parse error",resp,e);
            throw e;
        }

    }

    public static <T> BaseResponse postWithNamingStrategy(String url, Map<String, String> params, String content,
                                                          Class<T> targetClass,
                                                          PropertyNamingStrategy s,
                                                          Map<String, String> jsonTransferFiled, Boolean isList) {
        String resp = post(url, params, content);

        try {
            return dealHttpResult(resp, targetClass, s, jsonTransferFiled, isList);

        } catch (Exception e) {
            LOGGER.error("deal url ({}) json response parse error", url, e);
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
    private static String post(String url, Map<String, String> params, String content) {
        String response = null;
        try {
            response = HttpUtil.post(params, url, content, StandardCharsets.UTF_8.displayName());
        } catch (IOException e) {
            LOGGER.error("php connect url({}) exception", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
        if (response == null) {//正常条件get请求不应该返回空值
            LOGGER.warn("post url({}) repsonse null", url);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("call php url({}) resp({})", url, response);
        }
        return response;

    }

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

    public static void main(String[] args) throws IOException {
        String resp = "{\"code\":0,\"msg\":\"\",\"data\":[]}";
        if (resp.contains("\"msg\":")) {//返回结构不一样,需要转换一次
            final HashMap hashMap = JsonUtils.json2Bean(resp, HashMap.class);
            Object data = hashMap.get("data");
            final Object msg = hashMap.getOrDefault("msg", "");
            if (new Integer(0).equals(hashMap.get("code"))) {
                if (data != null && (data instanceof Collection)) {
                    if (CollectionUtils.isEmpty((Collection) data)) {
                        System.out.println("没数据直接返回");
                        System.out.println(JsonUtils.bean2Json(
                            new BaseResponse<>(Integer.valueOf(hashMap.get("code").toString()), msg.toString(), data)));
                        return;
                    }
                }
            }

            final Object oldMsg = hashMap.remove("msg");
            hashMap.put("message", oldMsg);
            resp = JsonUtils.bean2Json(hashMap);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        JavaType type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, HashMap.class);
        BaseResponse result = objectMapper.readValue(resp, type);
        System.out.println(JsonUtils.bean2Json(result));
    }

}
