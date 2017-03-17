package com.youzan.sz.common.util.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Created by zhanguo on 16/7/27.
 * 集成测试专用(例如远程rpc服务)
 */
@SuppressWarnings("SpringContextConfigurationInspection")
@ContextConfiguration(locations = "classpath:config-test-int.xml")
public abstract class BaseIntTest extends BaseSpringTest {
    
}
