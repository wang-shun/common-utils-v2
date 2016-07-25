package com.youzan.sz.monitor;

import com.youzan.sz.common.annotation.WithoutLogging;

/**
 *
 * Created by zhanguo on 16/7/19.
 * 心跳检测
 */

public interface HeathCheck {
    String HEATH_CHECK_TAG="heathCheckFlag";

    @WithoutLogging
    String heartBeat();
}
