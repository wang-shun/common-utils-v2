package com.youzan.sz.nsq;

/**
 *
 * Created by zhanguo on 16/8/1.
 * 消费者配置
 */
public class NSQConsRConfig {
    private int minThreadCount = 3;
    private int maxThreadCount = 3;
    private int queueSize      = 200;

    public int getMinThreadCount() {
        return minThreadCount;
    }

    public void setMinThreadCount(int minThreadCount) {
        this.minThreadCount = minThreadCount;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public void setMaxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }
}
