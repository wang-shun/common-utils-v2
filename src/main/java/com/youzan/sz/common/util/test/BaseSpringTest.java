package com.youzan.sz.common.util.test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseSpringTest.class);
    
    
    public static void initWithProfile(EnvProfile envProfile) {
        BaseTestUtil.initWithProfile(envProfile);
    }
    
}
