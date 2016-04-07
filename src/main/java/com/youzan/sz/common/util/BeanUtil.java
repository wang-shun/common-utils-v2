package com.youzan.sz.common.util;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: YANG
 * Date: 2015/12/17
 * Time: 20:03
 */
public class BeanUtil {

    public static <T> T copyProperty(Object source, Class<T> destinationClazz) {

        if (source == null) {
            return null;
        }

        T t = BeanUtils.instantiate(destinationClazz);
        BeanUtils.copyProperties(source, t);
        return t;
    }


    public static <T> T copyProperty(Object source, T t) {

        if (source == null) {
            return null;
        }

        BeanUtils.copyProperties(source, t);
        return t;
    }


    public static <T> List<T> copyPropertyList(List<?> sources, Class<T> destinationClazz) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(sources.size());
        for (Object obj : sources) {
            result.add(copyProperty(obj, destinationClazz));
        }
        return result;
    }
}
