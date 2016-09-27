package com.youzan.sz.common.util;

import com.youzan.sz.common.util.test.Task;
import com.youzan.sz.common.util.test.TestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 2016/9/23.
 */
public class TimeCostWrapper {
    private final static Logger LOGGER      = LoggerFactory.getLogger(TestWrapper.class);
    private int                 maxCostTime = 1000;
    private int                 count       = 1;

    public TimeCostWrapper() {
    }

    public TimeCostWrapper(int maxCostTime) {
        this.maxCostTime = maxCostTime;
    }

    //    /**
    //     *                     @param count 执行次数
    //     * */
    //    public TimeCostWrapper(int count, int maxCostTime) {
    //        this.count = count;
    //        this.maxCostTime = maxCostTime;
    //    }

    public static <T> T doTask(Task<T> task) {
        return doTask(task, 1000);
    }

    /**
     * @param maxCostTime  单位秒
     * */
    public static <T> T doTask(Task<T> task, Number maxCostTime) {
        return doTask(task, maxCostTime, 1);
    }

    /**
     * @param maxCostTime  单位秒
     *                     @param count 执行次数
     * */
    public static <T> T doTask(Task<T> task, Number maxCostTime, int count) {
        final TimeCostWrapper timeCostWrapper = new TimeCostWrapper();
        timeCostWrapper.maxCostTime = maxCostTime.intValue();
        timeCostWrapper.count = count;
        return timeCostWrapper.start(task);
    }

    public <T> T start(Task<T> task) {
        T t = null;
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            t = task.doTask();
            LOGGER.debug("finished count:{}", i);
        }
        long cost = System.currentTimeMillis() - start;
        String costStr;
        if (cost > 1000 && cost < 60 * 1000) {
            costStr = cost / 1000f + "(s)";
        } else if (cost < 60 * 1000 * 60) {
            costStr = cost / (1000f * 60) + "(min)";
        } else {
            costStr = cost + "(ms)";
        }

        if (cost / 1000 > maxCostTime) {
            LOGGER.warn("execute time cost:{}", costStr);
        } else {
            LOGGER.info("execute time cost:{}", costStr);
        }
        return t;
    }

}