package com.youzan.sz.common.util.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;


/**
 * Created by zhanguo on 16/7/27.
 * 单元测试专用
 * 如果需要测试远程service,{@link BaseIntTest}
 */
@RunWith(ExtendedSpringJUnit4ClassRunner.class)
@SuppressWarnings("SpringContextConfigurationInspection")
@ContextConfiguration(locations = "classpath:config-spring.xml")
public abstract class BaseSpringTest extends BaseTest {
    
    public static void initWithProfile(EnvProfile envProfile) {
        BaseTestUtil.initWithProfile(envProfile);
    }
    
}
