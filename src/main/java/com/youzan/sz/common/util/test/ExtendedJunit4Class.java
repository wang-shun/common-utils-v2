package com.youzan.sz.common.util.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.net.URL;

/**
 * Created by wangpan on 16/9/4.
 */
public class ExtendedJunit4Class extends BlockJUnit4ClassRunner {
    public ExtendedJunit4Class(Class<?> klass) throws InitializationError {
        super(klass);
        String simpleName = new BaseSpringTest.DefaultSpringTestConfig().getAppSimpleName();
        System.setProperty("app.full.name", "store-" + simpleName);
        System.setProperty("app.log.name", "store_" + simpleName);
        System.setProperty("app.name", simpleName);
        URL classPathURL = getClass().getClassLoader().getResource("");
        final String classPath = classPathURL.getFile();
        System.setProperty("props.path", classPath);
    }
}
