package com.youzan.sz.init;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.dubbo.DubboUtils;
import com.youzan.sz.monitor.HeathCheck;
import com.youzan.sz.monitor.HeathCheckImpl;

/**
 *
 * Created by zhanguo on 16/7/20.
 */
public class InitDistributedTools {
    private final static Logger  LOGGER  = LoggerFactory.getLogger(com.youzan.sz.init.InitDistributedTools.class);
    private static AtomicBoolean IS_INIT = new AtomicBoolean(false);

    public static void init() {
        try {
            if (IS_INIT.compareAndSet(false, true)) {
                LOGGER.info("InitDistributedTools start init...");
                initMonitor();
                LOGGER.info("InitDistributedTools end init...");
            }
        } catch (Throwable e) {
            LOGGER.info("init error:{}", e);
        }
    }

    /**初始化心跳服务*/
    private static void initMonitor() {
        LOGGER.info("开始初始化心跳服务");
        long startTime = System.currentTimeMillis();
        // 服务实现
        HeathCheck heathCheck = new HeathCheckImpl();
        DubboUtils.deployService(heathCheck);
        LOGGER.info("deploy monitor service:{} success", heathCheck.getClass().getCanonicalName());
        LOGGER.info("结束初始化心跳服务,耗时:{}(ms)", (System.currentTimeMillis() - startTime));
    }
}
