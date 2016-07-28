package com.youzan.sz.common.util.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * Created by zhanguo on 16/7/28.
 */
public enum EnvProfile {
                        DEV(BaseSpringTest.DEV_CONFIG), CI(BaseSpringTest.DEV_CONFIG), QA(BaseSpringTest.QA_CONFIG), PRE(BaseSpringTest.PROD_CONFIG), PROD(BaseSpringTest.PROD_CONFIG);

    private BaseSpringTest.SpringTestConfig springTestConfig;

    EnvProfile(BaseSpringTest.SpringTestConfig springTestConfig) {
        this.springTestConfig = springTestConfig;
    }

    public BaseSpringTest.SpringTestConfig getSpringTestConfig() {
        return springTestConfig;
    }
}
