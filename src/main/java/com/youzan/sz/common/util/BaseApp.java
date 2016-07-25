package com.youzan.sz.common.util;

import com.youzan.sz.init.InitDistributedTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * Created by zhanguo on 16/7/21.
 * 启动应用
 */
public abstract class BaseApp {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    private void initSpring() {
        // 启动Spring
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
            "classpath:config-spring.xml");
        applicationContext.start();
    }

    protected String getProjectName() {
        return this.getClass().getSimpleName();
    }


    protected void start() {
        preTask();
        doTask();
        logger.info("{} start running", getProjectName());
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
        initSpring();
        InitDistributedTools.init();//启动心跳
    }

    protected abstract void doTask();

    protected void afterTask() {
        logger.info("项目({})关闭", getProjectName());
    }
}