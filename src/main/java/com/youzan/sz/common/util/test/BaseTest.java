package com.youzan.sz.common.util.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.nashorn.internal.runtime.logging.Loggable;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 集成测试专用
 */
public abstract class BaseTest implements TestLoggable {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Logger getLogger() {
        return logger;
    }

}

interface TestLoggable {
    Logger getLogger();
}
