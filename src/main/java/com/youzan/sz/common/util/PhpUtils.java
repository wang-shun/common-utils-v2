package com.youzan.sz.common.util;

import static com.youzan.sz.jutil.string.StringUtil.UTF_8;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.response.enums.ResponseCode;


/**
 * Created by zhanguo on 16/8/2.
 * 处理调用php相关业务
 */
public class PhpUtils {
    
    private final static Boolean USE_HTTPS = false;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(PhpUtils.class);
    
    
    /**
     * 直接返回通过json解析数据为bean
     *
     * @param url 请求url
     * @param params get请求的heade参数,不需要encoding
     * @throws com.youzan.platform.bootstrap.exception.BusinessException 连接错误,解码错误
     */
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
                            LOGGER.debug("data no resp");
                            return new BaseResponse(Integer.valueOf(hashMap.get("code").toString()), msg.toString(), null);
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
    
    
    public static String get(String url, Map<String, String> header) {
        String response = null;
        try {
            response = HttpUtil.get(USE_HTTPS, header, url, StandardCharsets.UTF_8.displayName());
        } catch (IOException e) {
            LOGGER.error("php connect url({}) exception", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
        if (StringUtils.isEmpty(response)) {//正常条件get请求不应该返回空值
            LOGGER.warn("get url({}) repsonse null", url);
            return null;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("call php url({}) resp({})", url, response);
        }

        return response;
    }

    /**
     * 直接返回通过json解析数据为bean
     *
     * @param url 请求url
     * @throws com.youzan.platform.bootstrap.exception.BusinessException 连接错误,解码错误
     */
    public static <T> BaseResponse<T> getCamelResult(String url, Class<T> targetClass) {
        return getResultWithNamingStrategy(url, Collections.EMPTY_MAP, targetClass, PropertyNamingStrategy.SNAKE_CASE,
            Collections.EMPTY_MAP, false);
    }

    /**
     * 直接返回通过json解析数据为beangetget
     *
     * @param url 请求url
     * @param header get请求的heade参数,不需要encoding
     * @throws com.youzan.platform.bootstrap.exception.BusinessException 连接错误,解码错误
     */
    public static <T> BaseResponse<T> getResultWithNamingStrategy(String url, Map<String, String> header, Class<T> targetClass,

            PropertyNamingStrategy s, Map<String, String> jsonTransferFiled, Boolean isList) {
        String resp = get(url, header);
        if (StringUtils.isEmpty(resp)) {
            return null;
        }
        try {
            return dealHttpResult(resp, targetClass, s, jsonTransferFiled, isList);
        } catch (Exception e) {
            LOGGER.error("get url({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }
    
    
    private static <T> BaseResponse<T> dealHttpResult(String resp, Class<T> targetClass,

            PropertyNamingStrategy s, Map<String, String> jsonTransferFiled, Boolean isList) throws Exception {
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
                            LOGGER.debug("data no had  resp");
                            return new BaseResponse(Integer.valueOf(hashMap.get("code").toString()), msg.toString(), null);
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
            }else {
                type = objectMapper.getTypeFactory().constructParametricType(BaseResponse.class, targetClass);
            }
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            BaseResponse<T> result = objectMapper.readValue(resp, type);
            return result;
        } catch (Exception e) {
            LOGGER.error("deal with result ({}) parse error", resp, e);
            throw e;
        }

    }
    
    
    public static <T> BaseResponse postWithNamingStrategy(String url, Map<String, String> params, String content, Class<T> targetClass, PropertyNamingStrategy s, Map<String, String>
            jsonTransferFiled, Boolean isList) {
        String resp = post(url, params, content);

        try {
            return dealHttpResult(resp, targetClass, s, jsonTransferFiled, isList);

        } catch (Exception e) {
            LOGGER.error("deal url ({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }
    
    
    public static String post(String url, Map<String, String> params, String content) {
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
    
    
    public static <T> BaseResponse postResult(String url, Map<String, String> params, Class<T> clazz) {
        String resp = post(url, params);

        try {
            return dealHttpResult(resp, clazz, PropertyNamingStrategy.SNAKE_CASE, Collections.EMPTY_MAP, false);

        } catch (Exception e) {
            LOGGER.error("deal url ({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }
    
    
    public static String post(String url, Map<String, String> params) {
        String response;
        try {
            String paramsStr = null;
            if (CollectionUtils.isNotEmpty(params)) {
                List<NameValuePair> pairs = new ArrayList(params.size());
                for (Map.Entry<String, String> param : params.entrySet())
                    pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
                paramsStr = URLEncodedUtils.format(pairs, UTF_8.name());
            }
            if (url.endsWith("?"))
                url = url + paramsStr;
            else
                url = url + "?" + paramsStr;
            response = HttpUtil.post(Collections.EMPTY_MAP, url, paramsStr, StandardCharsets.UTF_8.displayName());
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
    
    
    /**
     * form表单提交的方式
     */
    public static <T> BaseResponse postFormResult(String url, Map<String, String> params, Class<T> clazz) {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        String resp = post(url, headers, params);

        try {
            return dealHttpResult(resp, clazz, PropertyNamingStrategy.SNAKE_CASE, Collections.EMPTY_MAP, false);

        } catch (Exception e) {
            LOGGER.error("deal url ({}) json response parse error", url, e);
            throw ResponseCode.ERROR.getBusinessException();
        }
    }
    
    
    private static String post(String url, Map<String, String> headers, Map<String, String> params) {
        String response = null;
        try {
            response = HttpUtil.post(url, headers, params);
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
                        System.out.println(JsonUtils.bean2Json(new BaseResponse<>(Integer.valueOf(hashMap.get("code").toString()), msg.toString(), data)));
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
