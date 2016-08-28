package com.youzan.sz.test.nsq;

import com.google.common.collect.Lists;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import com.youzan.sz.common.util.test.BaseJavaTest;
import com.youzan.sz.test.nsq.json.DemoStoreMsg;
import com.youzan.sz.test.nsq.protobuf.NSQProtoMsg;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *@TODO NSQ:是否支持一对多,消息过期的方式,自带版本的管理
 * Created by zhanguo on 16/7/29.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PropertiesUtils.class)
@PowerMockIgnore( {"javax.management.*"})
//@Ignore
public class NSQTest extends BaseJavaTest {

    @BeforeClass
    public static void setEnv() {

        mockStatic(PropertiesUtils.class);
        Mockito.when(
            PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host", "nsq-qa.s.qima-inc.com:4161"))
            .thenReturn("nsq-qa.s.qima-inc.com:4161");
        Mockito
            .when(PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.connection.timeout.second",//"nsq_connection_timeout_second",
                    String.valueOf(TimeUnit.SECONDS.toMillis(10))))
            .thenReturn(String.valueOf(TimeUnit.SECONDS.toMillis(20)));
    }

    @Test
    public void testStringPub() {
        DemoPubNsq demoPubNsq = new DemoPubNsq();
        demoPubNsq.register();

        DemoStoreMsg msg = new DemoStoreMsg();
        msg.setName("vincent");
        msg.setId(1);
        msg.setShopId(Lists.newArrayList(1, 2));

        NSQProtoMsg.NSQDemoProtoReq pubMsg = NSQProtoMsg.NSQDemoProtoReq.newBuilder().setName("vincent").setSex(1)
            .addShopId(2).build();

        Assert.assertTrue(demoPubNsq.pub(pubMsg));
    }

    @Test
    public void testStringConsumer() {

        new DemoConsRNSQ().register();
        try {
            TimeUnit.MINUTES.sleep(5L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
