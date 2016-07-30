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

/**
 *
 * Created by zhanguo on 16/7/29.
 */

public class Benchmark extends NSQTest {

}
