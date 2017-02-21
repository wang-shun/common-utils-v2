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

public class LongTermDelayExecutor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LongTermDelayExecutor.class);
    
    private static final ThreadPoolExecutor LONG_TERM_TASK_EXECUTOR;
    
    private static final DelayQueue<DelayItem> LONG_TERM_DELAY_QUEUE = new DelayQueue<>();
    
    private static final ExecutorService LOOP_EXECUTOR = Executors.newSingleThreadExecutor();
    
    static {
        int poolSize = Runtime.getRuntime().availableProcessors() + 1;
        LONG_TERM_TASK_EXECUTOR = new LongTermDelayThreadPool(1, poolSize, 45, TimeUnit.SECONDS, new LinkedBlockingDeque<>());
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
        delayItem.setDelayQueue(LONG_TERM_DELAY_QUEUE);
        LONG_TERM_DELAY_QUEUE.offer(delayItem);
    }
    
    
    @PreDestroy
    public void destroy() {
        LOOP_EXECUTOR.shutdown();
        LONG_TERM_TASK_EXECUTOR.shutdown();
        LOGGER.info("LongTermDelayTaskExecutor & LoopExecutor shutdown.");
    }
    
    
    @PostConstruct
    public void start() {
        LOOP_EXECUTOR.execute(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    DelayItem item = LONG_TERM_DELAY_QUEUE.take();
                    int executedTimes = item.getExecutedTimes();
                    if (executedTimes >= item.getMaxRetryTimes()) {
                        LOGGER.info("task [{}-{}] get the max time:[{}],end task.", item, item.getTaskName(), item.getMaxRetryTimes());
                    }else {
                        LONG_TERM_TASK_EXECUTOR.execute(item);
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        });
        LOGGER.info("LongTermDelayExecutor Loop Thread started...");
    }
    
    
    private static class LongTermDelayThreadPool extends ThreadPoolExecutor {
        
        public LongTermDelayThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
            setThreadFactory(new LongTermDelayThreadFactory());
            this.setRejectedExecutionHandler((r, executor) -> LOGGER.error("LongTermDelayThreadPool Rejected Runnable:{}", r));
        }
    }
    
    
    private static class LongTermDelayThreadFactory implements ThreadFactory {
        
        @Override
        public Thread newThread(Runnable r) {
            return new LongTermDelayThread(r);
        }
    }
    
    
    private static class LongTermDelayThread extends Thread {
        
        private static final String DEFAULT_NAME = "LongTermDelayThread";
        
        private static final AtomicLong created = new AtomicLong();
        
        
        LongTermDelayThread(Runnable target) {
            this(target, DEFAULT_NAME);
        }
        
        
        LongTermDelayThread(Runnable target, String name) {
            super(target, name + "-" + created.incrementAndGet());
            setUncaughtExceptionHandler((t, e) -> {
                if (e instanceof BusinessException) {
                    LOGGER.warn("{} Error:{}", t.getName(), e);
                }else {
                    LOGGER.error("{} Error:{}", t.getName(), e);
                }
            });
        }
    }
    
}




