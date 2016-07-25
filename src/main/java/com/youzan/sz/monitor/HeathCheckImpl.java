package com.youzan.sz.monitor;

/**
 *
 * Created by zhanguo on 16/7/19.
 * 心跳检测
 */

@com.alibaba.dubbo.config.annotation.Service
public class HeathCheckImpl implements HeathCheck {




    @Override public String heartBeat() {
        return "OK";
    }
}
