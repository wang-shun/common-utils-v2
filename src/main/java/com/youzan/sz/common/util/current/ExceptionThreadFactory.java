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
    private static final ThreadFactory            defaultFactory            = Executors.defaultThreadFactory();
    private final Thread.UncaughtExceptionHandler handler;
    private final static Logger                   LOGGER                    = LoggerFactory
        .getLogger(ExceptionThreadFactory.class);

    public static final ExceptionHandler          DEFAULT_EXCEPTION_HANDLER = new ExceptionHandler();
    public static final ExceptionThreadFactory    DEFAULT_EXCEPTION_FACTORY = new ExceptionThreadFactory(
        DEFAULT_EXCEPTION_HANDLER);

    public ExceptionThreadFactory(Thread.UncaughtExceptionHandler handler) {
        this.handler = handler;
    }

    @Override
    public Thread newThread(Runnable run) {
        Thread thread = defaultFactory.newThread(run);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        // ...

        @Override
        public void uncaughtException(Thread thread, Throwable t) {
            LOGGER.error("{},execute error", thread.getName(), t);
        }
    }

}
