package com.youzan.sz.common.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.youzan.sz.common.model.enums.BooleanEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by zefa on 16/6/23.
 */
public class JsonUtils {

    private static final Logger       LOGGER              = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper mapper              = new ObjectMapper();
    private static final ObjectMapper EXCLUDE_NULL_MAPPER = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        EXCLUDE_NULL_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        EXCLUDE_NULL_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        EXCLUDE_NULL_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);//默认不导出为空的字段

    }

    private JsonUtils() {
        throw new IllegalAccessError("Utility class");
    }


    public static String bean2Json(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("bean2Json ERROR ,object:{}", object, e);
            return null;
        }
    }


    public static JSONObject getPlainJsonString(String[]... objs) {
        if (objs != null) {
            final JSONObject jo = new JSONObject();
            for (String[] obj : objs) {
                jo.put(obj[0], obj[1]);
            }
            return jo;
        }
        return null;
    }


    public static JSONObject getSingleJson(String key, String value) {
        final JSONObject jo = new JSONObject(1);
        jo.put(key, value);
        return jo;
    }
    
    
    public static JSONObject getSingleJson(String key, Boolean value) {
        return getSingleJson(key, BooleanEnum.atoi(value));
    }


    public static JSONObject getSingleJson(String key, Number value) {
        final JSONObject jo = new JSONObject(1);
        jo.put(key, value);
        return jo;
    }
    
    
    public static <T> ArrayList<T> json2ListBean(String jsonStr, Class<T> elementClasses) {
        try {
            return mapper.readValue(jsonStr, getCollectionType(ArrayList.class, elementClasses));
        } catch (IOException e) {
            LOGGER.error("json2ListBean ERROR message", e);
            return null;
        }
    }
    
    
    private static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    public static void main(String[] args) {
        HashMap<String, String> result = JsonUtils.json2Bean("{\"name\":\"基德\",\"age\":99}", HashMap.class);
        System.out.println(result);
    }
    
    
    public static <T> T json2Bean(String jsonStr, Class<T> elementClasses) {
        if (String.class.equals(elementClasses)) {
            return (T) jsonStr;
        }
        try {
            return mapper.readValue(jsonStr, elementClasses);
        } catch (IOException e) {
            LOGGER.warn("json to bean error,source json:{} ", jsonStr, e);
            return null;
        }
    }
    
    
    public static String toJson(Object object) {
        try {
            if (object instanceof String) {
                return String.valueOf(object);
            }
            return EXCLUDE_NULL_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("bean2Json ERROR ,object:{}", object, e);
            return null;
        }
        
    }

}
