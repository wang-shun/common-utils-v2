package com.youzan.sz.common.util.test;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * Created by zhanguo on 16/7/27.
 * 单元测试专用,如果需要不同测试环境需要继承不同父类.see{@link BaseSpringDevTest}
 */

@TestProfile(EnvProfile.QA)
public abstract class BaseSpringQATest extends BaseSpringTest {
    
    @BeforeClass
    public static void init() {
        BaseSpringTest.initWithProfile(BaseSpringQATest.class.getAnnotation(TestProfile.class).value());
    }
    
}
