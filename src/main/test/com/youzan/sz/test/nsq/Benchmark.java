package com.youzan.sz.test.nsq;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.youzan.nsq.client.exception.NSQInvalidTopicException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Lists;
import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;
import com.youzan.sz.common.util.test.BaseJavaTest;
import com.youzan.sz.test.nsq.json.DemoStoreMsg;
import com.youzan.sz.test.nsq.protobuf.NSQProtoMsg;

import java.util.concurrent.TimeUnit;

/**
 *
 * Created by zhanguo on 16/7/29.
 */

public class Benchmark extends NSQTest {
    @Test
    public void benchmarkPub() {
        DemoPubNsq demoPubNsq = new DemoPubNsq();
        demoPubNsq.register();

        DemoStoreMsg msg = new DemoStoreMsg();
        msg.setName("vincent");
        msg.setId(1);
        msg.setShopId(Lists.newArrayList(1, 2));

        NSQProtoMsg.NSQDemoProtoReq pubMsg = NSQProtoMsg.NSQDemoProtoReq.newBuilder().setName("vincent").setSex(1)
            .addShopId(2).build();
        new TimeCost(10000) {
            @Override
            public void run() {
                demoPubNsq.pub(pubMsg);
            }
        };

    }

    @Test
    public void benchmarkConsR() {
        new DemoConsRNSQ().register();
        try {
            TimeUnit.HOURS.sleep(1L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
