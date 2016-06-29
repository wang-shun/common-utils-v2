package com.youzan.sz.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by zefa on 16/6/23.
 */
public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    private static JavaType getCollectionType(Class<?>  collectionClass, Class<?> elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    public static <T> ArrayList<T> json2ListBean(String jsonStr, Class<T> elementClasses) throws IOException {
        return mapper.readValue(jsonStr, getCollectionType(ArrayList.class, elementClasses));
    }

    public static String bean2Json(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static <T> T json2Bean(String jsonStr, Class<T> elementClasses) throws IOException {
        return mapper.readValue(jsonStr, elementClasses);
    }
}
