package com.youzan.sz.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zefa on 16/6/23.
 */
public class JsonUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper mapper = new ObjectMapper();

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
            LOGGER.error("bean2Json ERROR object =" + object.toString());
            return null;
        }
    }

    public static <T> T json2Bean(String jsonStr, Class<T> elementClasses) {
        try {
            return mapper.readValue(jsonStr, elementClasses);
        } catch (IOException e) {
            LOGGER.error("json2Bean ERROR jsonStr =" + jsonStr);
            return null;
        }
    }

    public static String getPlainJsonString(String[]... objs) {
        if (objs != null) {
            Map<String, String> jo = new HashMap<>();
            for (String[] obj : objs) {
                jo.put(obj[0], obj[1]);
            }
            return bean2Json(jo);
        }
        return null;
    }

}
