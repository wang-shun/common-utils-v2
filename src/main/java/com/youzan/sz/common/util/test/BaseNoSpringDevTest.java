package com.youzan.sz.common.util.test;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Created by wangpan on 16/9/4.
 */
@RunWith(ExtendedJunit4Class.class)
@TestProfile(EnvProfile.DEV)
public abstract  class BaseNoSpringDevTest extends BaseNoSpringTest {
    @BeforeClass
    public static void init() {

        BaseNoSpringTest.initWithProfile(BaseNoSpringDevTest.class.getAnnotation(TestProfile.class).value());
    }
}
