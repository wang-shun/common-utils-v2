package com.youzan.sz.common.util;

import com.youzan.sz.common.annotation.JacksonField;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Created by wangpan on 2016/10/17.
 */
@Deprecated()
public class JacksonFieldUtil {

    public static Map<String, String> getJsonFiledsValue(Class targetClass) {


        Map<String, String> map = new HashMap<>();

        Queue<Class> queue = new LinkedList<Class>();
        queue.offer(targetClass);
        if (queue.size() == 0) {
            return map;
        }
        while (true) {
            Class target = queue.poll();
            if (target == null) {
                break;
            }
            Field[] fields = target.getDeclaredFields();
            target.getDeclaredClasses();
            for (Field f : fields) {
               queue.add(f.getGenericType().getClass());
                Annotation annotation = f.getAnnotation(JacksonField.class);
                if (annotation != null) {
                    JacksonField jacksonField = (JacksonField) annotation;
                    if (StringUtils.isNotEmpty(jacksonField.value())) {
                        map.put(f.getName(), jacksonField.value());
                    }
                }
            }

        }
        return map;
    }
}
