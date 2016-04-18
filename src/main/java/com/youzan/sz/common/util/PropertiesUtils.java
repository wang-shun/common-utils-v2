package com.youzan.sz.common.util;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zefa on 16/4/18.
 */
public class PropertiesUtils {
    private static Map<String, Map<String, String>> map = new HashMap<>();

    /**
     * 获取配置
     *
     * @param propertiesName 配置文件名称
     * @param propertiesKey  配置名称
     * @return 配置值
     */
    public static String getProperty(String propertiesName, String propertiesKey) throws BusinessException {
        //判断是否命中缓存,命中则从缓存中读取
        if (map.get(propertiesName) == null) {
            //未命中缓存则读取配置文件
            Properties prop = new Properties();
            try {
                Map<String, String> propertiesMap = new HashMap<>();
                //读取属性文件a.properties
                InputStream in = new BufferedInputStream(new FileInputStream(propertiesName));
                prop.load(in);     ///加载属性列表
                Iterator<String> it = prop.stringPropertyNames().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    propertiesMap.put(key, prop.getProperty(key));
                }
                map.put(propertiesName, propertiesMap);
                in.close();
            } catch (Exception e) {
                throw new BusinessException((long) ResponseCode.ERROR.getCode(), e.getMessage());
            }
        }
        return map.get(propertiesName).get(propertiesKey);
    }

    /**
     * 获取配置,获取失败则返回默认值
     *
     * @param propertiesName 配置文件名称
     * @param propertiesKey  配置名称
     * @param defaultValue   默认值
     * @return
     */
    public static String getProperty(String propertiesName, String propertiesKey, String defaultValue) {
        try {
            return getProperty(propertiesName, propertiesKey);
        } catch (BusinessException e) {
            return defaultValue;
        }
    }
}
