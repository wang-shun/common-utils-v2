package com.youzan.sz.common.util;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by zefa on 16/4/18.
 */
public class PropertiesUtils {
    private static final ConcurrentHashMap<String, Future<Map<String, String>>> MAP    = new ConcurrentHashMap<>();
    private static final Logger                                                 LOGGER = LoggerFactory
        .getLogger(PropertiesUtils.class);

    /**
     * 获取配置
     *
     * @param fileName      配置文件名称
     * @param propertiesKey 配置名称
     * @return 配置值
     */
    public static String getProperty(String fileName, String propertiesKey) throws BusinessException {
        Future<Map<String, String>> future = MAP.get(fileName);

        //判断是否命中缓存,命中则从缓存中读取
        if (future == null) {
            // 未命中,由当前线程加载
            FutureTask<Map<String, String>> futureTask = new FutureTask<>(() -> {
                Properties prop = new Properties();
                InputStream inputStream = null;
                try {
                    Map<String, String> propertiesMap = new HashMap<>();
                    inputStream = new FileInputStream(fileName);
                    prop.load(inputStream);
                    for (String key : prop.stringPropertyNames()) {
                        propertiesMap.put(key, prop.getProperty(key));
                    }
                    return propertiesMap;
                } catch (Exception e) {
                    LOGGER.error("Read Property Exception:{}", e);
                    throw new BusinessException((long) ResponseCode.ERROR.getCode(), e.getMessage());
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            LOGGER.error("Read Property Exception:{}", e);
                        }
                    }
                }
            });

            // 看是否由其他线程给先加载了该属性配置文件
            future = MAP.putIfAbsent(fileName, futureTask);
            if (future == null) { // 没人加载过,把自己放到MAP中,并执行
                future = futureTask;
                futureTask.run();
            }
        }

        try {
            return future.get().get(propertiesKey);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Read Property Exception:{}", e);
            throw new BusinessException((long) ResponseCode.ERROR.getCode(), e.getMessage());
        }
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
        String property;
        try {
            property = getProperty(propertiesName, propertiesKey);
            if (property == null) {
                LOGGER.error("load property({}) is null,use default value:{}", propertiesName, defaultValue);

                return defaultValue;
            }
        } catch (Throwable e) {
            LOGGER.error("load property({}) error,use default value:{}", propertiesName, defaultValue);
            property = defaultValue;
        }
        return property;
    }

    public static Integer getInteger(String file, String key) {
        final String property = getProperty(file, key);
        return Integer.valueOf(property);
    }

}
