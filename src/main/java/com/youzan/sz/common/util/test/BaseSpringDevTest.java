package com.youzan.sz.common.util.test;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 单元测试专用
 * 如果需要测试远程service,{@link BaseIntTest}
 *
 */

@SuppressWarnings("SpringContextConfigurationInspection")
@RunWith(ExtendedSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:config-spring.xml")
@TestProfile(EnvProfile.DEV)
public abstract class BaseSpringDevTest extends BaseSpringTest {
    @BeforeClass
    public static void init() {
        BaseSpringTest.initWithProfile(BaseSpringDevTest.class.getAnnotation(TestProfile.class).value());
    }

}
