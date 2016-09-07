package com.youzan.sz.common.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.youzan.sz.common.util.test.Jsonbean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zefa on 16/6/23.
 */
public class JsonUtils {

    private static final Logger       LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private JsonUtils() {
        throw new IllegalAccessError("Utility class");
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?> elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static <T> ArrayList<T> json2ListBean(String jsonStr, Class<T> elementClasses) {
        try {
            return mapper.readValue(jsonStr, getCollectionType(ArrayList.class, elementClasses));
        } catch (IOException e) {
            LOGGER.error("json2ListBean ERROR message =" + e.getMessage());
            return null;
        }
    }

    public static String bean2Json(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("bean2Json ERROR ", e);
            return null;
        }
    }

    public static <T> T json2Bean(String jsonStr, Class<T> elementClasses) {
        try {
            if (elementClasses.newInstance() instanceof String) {
                return (T) jsonStr;
            }
            return mapper.readValue(jsonStr, elementClasses);
        } catch (Exception e) {
            LOGGER.error("json2Bean ERROR ", e);
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

    public static void main(String[] args) {

        System.out.println(JsonUtils.json2Bean("{\"name\":\"基德\",\"age\":99}", Jsonbean.class));
    }

}
