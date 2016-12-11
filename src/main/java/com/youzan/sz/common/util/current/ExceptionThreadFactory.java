package com.youzan.sz.common.util.current;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/9/13.
 */
public class ExceptionThreadFactory implements ThreadFactory {
    private static final ThreadFactory            DEFAULT_FACTORY           = Executors.defaultThreadFactory();
    private ThreadFactory                         threadFactory;
    private final Thread.UncaughtExceptionHandler handler;
    private final static Logger                   LOGGER                    = LoggerFactory
        .getLogger(ExceptionThreadFactory.class);

    public static final ExceptionHandler          DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler();
    public static final ExceptionThreadFactory    DEFAULT_EXCEPTION_FACTORY = new ExceptionThreadFactory();

    public ExceptionThreadFactory() {
        this.handler = DEFAULT_EXCEPTION_HANDLER;
        threadFactory = DEFAULT_FACTORY;
    }

    public ExceptionThreadFactory(ExceptionHandler exceptionHandler) {
        this.handler = exceptionHandler;
        threadFactory = DEFAULT_FACTORY;
    }

    public ExceptionThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        handler = DEFAULT_EXCEPTION_HANDLER;
    }

    @Override
    public Thread newThread(Runnable run) {
        Thread thread = threadFactory.newThread(run);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        // ...

        @Override
        public void uncaughtException(Thread thread, Throwable t) {
            LOGGER.warn("{},execute error", thread.getName(), t);
        }
    }


}
