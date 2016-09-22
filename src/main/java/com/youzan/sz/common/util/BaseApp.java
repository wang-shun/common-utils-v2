package com.youzan.sz.common.util;

import com.youzan.sz.common.interfaces.DevModeEnable;
import com.youzan.sz.common.util.current.ExceptionThreadFactory;
import com.youzan.sz.common.util.test.TestWrapper;
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

/**
 *
 * Created by zhanguo on 16/7/21.
 * 启动应用
 */
public abstract class BaseApp implements DevModeEnable {

    protected Logger                              logger             = LoggerFactory.getLogger(getClass());
    private static ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
        "classpath:config-spring.xml");
    private final List<Runnable>                  asyncTasks         = new ArrayList<>();
    private final ExecutorService                 executorService    = Executors.newFixedThreadPool(1,
        ExceptionThreadFactory.DEFAULT_EXCEPTION_FACTORY);;

    private void initSpring() {
        // 启动Spring
        applicationContext.start();
    }

    protected String getProjectName() {
        return this.getClass().getSimpleName();
    }

    protected void start() {
        preTask();
        doTask();
        logger.info("{} start running", getProjectName());
        if (isDevModel())
            new TestWrapper(() -> getProjectName());
        if (asyncTasks.size() > 0) {
            logger.info("开始执行异步任务");
            for (Runnable asyncTask : asyncTasks) {
                final Future<?> submit = executorService.submit(asyncTask);
            }
        }
        try {
            synchronized (getClass()) {
                this.getClass().wait();
            }
        } catch (InterruptedException e) {
            logger.info("{}:wait exception", getProjectName(), e);
        } finally {
            afterTask();
        }

    }

    protected void preTask() {

        //startJvmMonitor();
        initSpring();
        //addHook();
        // com.alibaba.dubbo.container.Main.main(new String[]{});
        InitDistributedTools.init();//启动心跳
    }

    protected void addHook() {
        String HOOK_NAME = "hook";
        ExtensionLoader<Container> loader = ExtensionLoader.getExtensionLoader(Container.class);

        if (loader.getExtension(HOOK_NAME) != null) {
            loader.getExtension(HOOK_NAME).start();
        }
    }

    protected void startJvmMonitor() {
        Config config = new Config();
        String appName = ContainerConfig.get("application.name");
        config.setApplication(appName);
        config.setUrl(ContainerConfig.getHawkAddr());
        Monitors.getInstance().startJvmMonitor(config);

    }

    protected abstract void doTask();

    //异步任务
    protected BaseApp addAsyncTask(Runnable runnable) {
        asyncTasks.add(runnable);
        return this;
    }

    protected void afterTask() {
        logger.info("项目({})关闭", getProjectName());
    }
}
