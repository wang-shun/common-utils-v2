package com.youzan.sz.common.util.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import com.youzan.sz.common.util.PropertiesUtils;
import org.junit.runners.model.InitializationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.FileUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 单元测试专用
 * 如果需要测试远程service,{@link BaseIntTest}
 *
 */

public class ExtendedSpringJUnit4ClassRunner extends SpringJUnit4ClassRunner {

    /**
     * Construct a new {@code SpringJUnit4ClassRunner} and initialize a
     * {@link TestContextManager} to provide Spring testing functionality to
     * standard JUnit tests.
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public ExtendedSpringJUnit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        String simpleName = new BaseTestUtil.DefaultTestConfig().getAppSimpleName();
        System.setProperty("app.full.name", "store-" + simpleName);
        System.setProperty("app.log.name", "store_" + simpleName);
        System.setProperty("app.name", simpleName);
        URL classPathURL = getClass().getClassLoader().getResource("");
        final String classPath = classPathURL.getFile();
        System.setProperty("props.path", classPath);

    }
}
