package com.youzan.sz.common.util;

import com.youzan.sz.common.interfaces.DevModeEnable;
import com.youzan.sz.common.util.current.ExceptionThreadFactory;
import com.youzan.sz.common.util.test.TestWrapper;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.container.Container;
import com.youzan.hawk.collect.utils.Config;
import com.youzan.hawk.collect.v2.monitor.Monitors;
import com.youzan.platform.bootstrap.common.ContainerConfig;
import com.youzan.sz.init.InitDistributedTools;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;


/**
 * Created by zhanguo on 16/7/21. 启动应用
 */
public abstract class BaseApp implements DevModeEnable {

    private static ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:config-spring.xml");

    private final List<Runnable> asyncTasks = new ArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(new ExceptionThreadFactory(new BasicThreadFactory.Builder().namingPattern("base-app-%d").build()));

    protected Logger logger = LoggerFactory.getLogger(getClass());


    //异步任务
    protected BaseApp addAsyncTask(Runnable runnable) {
        asyncTasks.add(runnable);
        return this;
    }


    protected void start() {
        preTask();
        doTask();
        logger.info("{} start running", getProjectName());
        if (isDevModel())
            new TestWrapper(this::getProjectName);
        if (asyncTasks.size() > 0) {
            logger.info("start execute async task");
            for (Runnable asyncTask : asyncTasks) {
                try {
                    executorService.execute(asyncTask);
                } catch (Exception e) {
                    logger.warn("async execute error", e);
                }
            }
        }
        try {
            synchronized (BaseApp.class) {
                BaseApp.class.wait();
            }
        } catch (InterruptedException e) {
            logger.info("{}:wait exception", getProjectName(), e);
        } finally {
            afterTask();
        }

    }


    protected void preTask() {
        //初始化公用配置
        initCommonConfigs();
        //startJvmMonitor();
        initSpring();
        addHook();
        // com.alibaba.dubbo.container.Main.main(new String[]{});
        InitDistributedTools.init();//启动心跳
    }


    protected abstract void doTask();


    protected String getProjectName() {
        return this.getClass().getSimpleName();
    }


    protected void afterTask() {
        logger.info("项目({})关闭", getProjectName());
    }


    protected void initCommonConfigs() {
        //设置通用异常处理器
        Thread.setDefaultUncaughtExceptionHandler(ExceptionThreadFactory.DEFAULT_EXCEPTION_HANDLER);
    }


    private void initSpring() {
        // 启动Spring
        applicationContext.start();
    }


    protected void addHook() {
        try {
            String HOOK_NAME = "hook";
            ExtensionLoader<Container> loader = ExtensionLoader.getExtensionLoader(Container.class);

            if (loader.getExtension(HOOK_NAME) != null) {
                loader.getExtension(HOOK_NAME).start();
            }
        } catch (Throwable e) {
            logger.warn("add hook error", e);
        }
    }


    protected void startJvmMonitor() {
        Config config = new Config();
        String appName = ContainerConfig.get("application.name");
        config.setApplication(appName);
        config.setUrl(ContainerConfig.getHawkAddr());
        Monitors.getInstance().startJvmMonitor(config);

    }
}
