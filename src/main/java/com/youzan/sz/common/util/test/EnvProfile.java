package com.youzan.sz.common.util.test;

/**
 *
 * Created by zhanguo on 16/7/28.
 */
public enum EnvProfile {
                        DEV(BaseTestConf.DEV_CONFIG), CI(BaseTestConf.DEV_CONFIG), QA(BaseTestConf.QA_CONFIG), PRE(BaseTestConf.PROD_CONFIG), PROD(BaseTestConf.PROD_CONFIG);

    private BaseTestConf baseTestConf;

    EnvProfile(BaseTestConf baseTestConfig) {
        this.baseTestConf = baseTestConfig;
    }

    public BaseTestConf getBaseTestConfig() {
        return baseTestConf;
    }
}
