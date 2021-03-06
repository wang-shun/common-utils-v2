package com.youzan.sz.common.delay;

import com.youzan.platform.bootstrap.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;


/**
 * Created by Kid on 16/5/26.
 */

public class DelayExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayExecutor.class);
    
    private static final ThreadPoolExecutor TASK_EXECUTOR;
    
    private static final DelayQueue<DelayItem> DELAY_QUEUE = new DelayQueue<>();
    
    private static final ExecutorService LOOP_EXECUTOR = Executors.newSingleThreadExecutor();
    
    static {
        int poolSize = Runtime.getRuntime().availableProcessors() + 1;
        TASK_EXECUTOR = new DelayThreadPool(poolSize, poolSize, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
    }
    
    public void addTask(String taskName, Runnable runnable, int maxRetryTimes, long delayTime, TimeUnit timeUnit) {
        addTask(taskName, runnable, maxRetryTimes, delayTime, timeUnit, DelayItem.DelayPolicy.MULTIPLY_BY_EXECUTED_TIMES);
    }
    
    
    public void addTask(String taskName, Runnable runnable, int maxRetryTimes, long delayTime, TimeUnit timeUnit, DelayItem.DelayPolicy delayPolicy) {
        long delayTimeInMillis = TimeUnit.MILLISECONDS.convert(delayTime, timeUnit);
        DelayItem.DefaultDelayItem delayItem = new DelayItem.DefaultDelayItem(taskName, (int) delayTimeInMillis, maxRetryTimes, runnable, delayPolicy);
        addTask(delayItem);
    }
    
    
    public void addTask(DelayItem delayItem) {
        delayItem.setDelayQueue(DELAY_QUEUE);
        DELAY_QUEUE.offer(delayItem);
    }
    
    
    @PreDestroy
    public void destroy() {
        LOOP_EXECUTOR.shutdown();
        TASK_EXECUTOR.shutdown();
        
        LOGGER.info("DelayTaskExecutor & LoopExecutor shutdown.");
    }
    
    
    @PostConstruct
    public void start() {
        LOOP_EXECUTOR.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DelayItem item = DELAY_QUEUE.take();
                    if (item.getExecutedTimes() >= item.getMaxRetryTimes()) {
                        LOGGER.info("task [{}-{}] have already try:[{}] times,end task.", item, item.getTaskName(), item.getMaxRetryTimes());
                    }else {
                        TASK_EXECUTOR.execute(item);
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        LOGGER.info("DelayExecutor Loop Thread started...");
    }
    
    
    private static class DelayThreadPool extends ThreadPoolExecutor {
        
        public DelayThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            setThreadFactory(new DelayThreadFactory());
            this.setRejectedExecutionHandler((r, executor) -> LOGGER.error("DelayThreadPool Rejected Runnable:{}", r));
        }
    }
    
    
    private static class DelayThreadFactory implements ThreadFactory {
        
        @Override
        public Thread newThread(Runnable r) {
            return new DelayThread(r);
        }
    }
    
    
    private static class DelayThread extends Thread {
        
        private static final String DEFAULT_NAME = "DelayThread";
        
        private static final AtomicLong created = new AtomicLong();
        
        
        DelayThread(Runnable target) {
            this(target, DEFAULT_NAME);
        }
        
        
        DelayThread(Runnable target, String name) {
            super(target, name + "-" + created.incrementAndGet());
            setUncaughtExceptionHandler((t, e) -> {
                LOGGER.info("An exception occurred. Thread name:{}", t.getName());
                if (e instanceof BusinessException) {
                    LOGGER.warn("Error message:", e);
                }else {
                    LOGGER.error("Error message:", e);
                }
            });
        }
    }
    
}




