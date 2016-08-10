package com.youzan.sz.common.util;

import org.slf4j.LoggerFactory;

/**
 * Created by Kid on 16/8/10.
 */
public class LogFactory {

    public static Log getLog(Class<?> clazz) {
        return new Log(LoggerFactory.getLogger(clazz));
    }

    public static Log getLog(String name) {
        return new Log(LoggerFactory.getLogger(name));
    }
}
