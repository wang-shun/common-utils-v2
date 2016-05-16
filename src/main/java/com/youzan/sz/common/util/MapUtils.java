package com.youzan.sz.common.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zefa on 16/5/16.
 */
public class MapUtils {
    public static Long getLong(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        } else {
            if (obj instanceof List) {
                obj = ((List) obj).get(0);
            }
            String str = String.valueOf(obj);
            return Long.parseLong(str);
        }
    }

    public static Long getLongOrDefault(Map<String, Object> map, String key, Long defaultValue) {
        Long val = getLong(map, key);
        if (val == null) {
            val = defaultValue;
        }
        return val;
    }

    public static Integer getInt(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        } else {
            String str = String.valueOf(obj);
            return Integer.parseInt(str);
        }
    }

    public static Integer getIntOrDefault(Map<String, Object> map, String key, Integer defaultValue) {
        Integer val = getInt(map, key);
        if (val == null) {
            val = defaultValue;
        }
        return val;
    }

    public static String getString(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        } else {
            return String.valueOf(obj);
        }
    }

    public static String getStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
        String str = getString(map, key);
        if (str == null) {
            str = defaultValue;
        }
        return str;
    }

    public static Date getDate(Map<String, Object> map, String key) {
        Object obj = map.get(key);
        if (obj == null) {
            return null;
        } else {
            return (Date) obj;
        }
    }

    public static Date getDateOrDefault(Map<String, Object> map, String key, Date date) {
        Date d = getDate(map, key);
        if (d == null) {
            d = date;
        }
        return d;
    }
}
