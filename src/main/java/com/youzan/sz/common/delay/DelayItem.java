package com.youzan.sz.common.delay;

import com.youzan.platform.bootstrap.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by Kid on 16/5/26.
 */
public abstract class DelayItem<R> implements Runnable, Delayed, Serializable {
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(DelayItem.class);
    
    private final List<Listener<R>> successListeners = new ArrayList<>(); // 操作成功的监听
    
    private final List<Listener> exceptionListeners = new ArrayList<>(); // 操作异常的监听
    
    private final int maxRetryTimes; // 任务最多执行时间
    
    private final String taskName;
    
    private final AtomicInteger executedTimes = new AtomicInteger(0);  // 已执行次数
    
    private final DelayPolicy delayPolicy;
    
    private DelayQueue<DelayItem> delayQueue;  // 放到对应的延时队列中
    
    private long baseTime; // 基于这个时间,延时
    
    private int delayTimeInMillis; // 延时时间执行时间,毫秒
    
    
    public DelayItem(String taskName, int delayTimeInMillis, int maxRetryTimes, DelayPolicy delayPolicy) {
        if (maxRetryTimes <= 0) {
            maxRetryTimes = Integer.MAX_VALUE;
        }
        this.taskName = taskName;
        this.maxRetryTimes = maxRetryTimes;
        this.delayTimeInMillis = delayTimeInMillis;
        this.baseTime = System.currentTimeMillis();
        this.delayPolicy = delayPolicy;
    }
    
    
    public DelayItem(String taskName, int delayTimeInMillis, int maxRetryTimes) {
        if (maxRetryTimes <= 0) {
            maxRetryTimes = Integer.MAX_VALUE;
        }
        this.taskName = taskName;
        this.maxRetryTimes = maxRetryTimes;
        this.delayTimeInMillis = delayTimeInMillis;
        this.baseTime = System.currentTimeMillis();
        this.delayPolicy = DelayPolicy.MULTIPLY_BY_EXECUTED_TIMES;
    }
    
    
    // 异常的通知
    protected void addExceptionListener(Listener listener) {
        exceptionListeners.add(listener);
    }
    
    
    // 成功的通知
    protected void addSuccessListner(Listener<R> listener) {
        successListeners.add(listener);
    }
    
    
    protected void clearExceptionListeners() {
        exceptionListeners.clear();
    }
    
    
    protected void clearSuccessListners() {
        successListeners.clear();
    }
    
    
    @Override
    public int compareTo(Delayed o) {
        if (o == this) {
            return 0;
        }
        
        long diff = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
        return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1);
    }
    
    
    @Override
    public long getDelay(TimeUnit unit) {
        if (this.getDelayPolicy().equals(DelayPolicy.MULTIPLY_BY_EXECUTED_TIMES)) {
            return unit.convert((this.getExecutedTimes() + 1) * delayTimeInMillis + baseTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }else {
            return unit.convert(delayTimeInMillis + baseTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }
    
    
    public DelayPolicy getDelayPolicy() {
        return delayPolicy;
    }
    
    
    public int getExecutedTimes() {
        return executedTimes.get();
    }
    
    
    protected int getDelayTimeInMillis() {
        return delayTimeInMillis;
    }
    
    
    public void setDelayTimeInMillis(int delayTimeInMillis) {
        this.delayTimeInMillis = delayTimeInMillis;
    }
    
    
    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }
    
    
    protected void notifyExceptionListeners(BusinessException be) {
        exceptionListeners.forEach(listener -> listener.onException(be));
    }
    
    
    protected void notifySuccessListeners(R r) {
        successListeners.forEach(listener -> listener.onSuccess(r));
    }
    
    
    protected void removeExceptionListener(Listener listener) {
        exceptionListeners.remove(listener);
    }
    
    
    protected void removeSuccessListner(Listener<R> listener) {
        successListeners.remove(listener);
    }
    
    
    @Override
    public final void run() {
        try {
            this.increaseExecutedTimes();
            LOGGER.info("Task [{}-{}] already executed [{}] times.", this, this.getTaskName(), getExecutedTimes());
            doRun();
        } catch (BusinessException be) {
            LOGGER.warn("Task[{}-{}] executed failed, re-enqueue , already executed [{}] times.", this, this.getTaskName(), this.getExecutedTimes(), be);
            retry();
        } catch (Exception e) {
            LOGGER.error("Task[{}-{}] executed failed,re-enqueue, already executed [{}] times.", this, this.getTaskName(), this.getExecutedTimes(), e);
            retry();
        }
    }
    
    
    private int increaseExecutedTimes() {
        return executedTimes.incrementAndGet();
    }
    
    
    public String getTaskName() {
        return taskName;
    }
    
    
    public abstract void doRun();
    
    
    protected void retry() {
        this.setBaseTime(System.currentTimeMillis());
        this.delayQueue.offer(this);
    }
    
    
    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }
    
    
    void setDelayQueue(DelayQueue<DelayItem> delayQueue) {
        this.delayQueue = delayQueue;
    }
    
    
    public enum DelayPolicy {
        CONSTANT,
        MULTIPLY_BY_EXECUTED_TIMES
    }
    
    
    /**
     * 成功、失败的结果监听
     */
    
    public interface Listener<T> extends Serializable {
        
        default void onException(BusinessException be) {
            LOGGER.warn("Exception:{}", be);
        }
        
        default void onSuccess(T t) {
            throw new UnsupportedOperationException("方法未实现");
        }
    }
    
    
    /**
     * 默认的延时任务
     */
    public static class DefaultDelayItem<R> extends DelayItem<R> {
        
        private final Runnable runnable;
        
        
        public DefaultDelayItem(String taskName, int delayTimeInMillis, int maxRetryTimes, Runnable runnable) {
            super(taskName, delayTimeInMillis, maxRetryTimes, DelayPolicy.MULTIPLY_BY_EXECUTED_TIMES);
            this.runnable = runnable;
        }
        
        
        public DefaultDelayItem(String taskName, int delayTimeInMillis, int maxRetryTimes, Runnable runnable, DelayPolicy delayPolicy) {
            super(taskName, delayTimeInMillis, maxRetryTimes, delayPolicy);
            this.runnable = runnable;
        }
        
        
        @Override
        public void doRun() {
            runnable.run();
        }
    }
}



