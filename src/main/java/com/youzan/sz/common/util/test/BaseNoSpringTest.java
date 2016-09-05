package com.youzan.sz.common.util.test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangpan on 16/9/4.
 */
@RunWith(ExtendedJunit4Class.class)
public abstract class BaseNoSpringTest extends BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseSpringTest.class);

    public static void initWithProfile(EnvProfile envProfile) {
        BaseTestUtil.initWithProfile(envProfile);
    }

}


