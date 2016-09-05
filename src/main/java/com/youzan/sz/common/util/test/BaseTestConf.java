package com.youzan.sz.common.util.test;

/**
 * Created by wangpan on 16/9/5.
 */
public interface BaseTestConf {
    String getAppSimpleName();

    String getPropertyName();

    static final BaseTestConf QA_CONFIG   = new BaseTestUtil.DefaultTestConfig().setProperty("qa.properties");
    static final BaseTestConf PROD_CONFIG = new BaseTestUtil.DefaultTestConfig().setProperty("prod.properties");
    static final BaseTestConf CI_CONFIG   = new BaseTestUtil.DefaultTestConfig().setProperty("ci.properties");
    static final BaseTestConf DEV_CONFIG  = new BaseTestUtil.DefaultTestConfig().setProperty("dev.properties");
}
