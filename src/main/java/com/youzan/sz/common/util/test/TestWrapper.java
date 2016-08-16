package com.youzan.sz.common.util.test;

import com.youzan.sz.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/7/27.
 */
public class TestWrapper implements TestLoggable {

    final static Logger    logger = LoggerFactory.getLogger(TestWrapper.class);
    private final TestTask testTask;

    public TestWrapper(TestTask testTask) {
        this.testTask = testTask;
        this.start();
    }

    Object start() {
        prettyStart();
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = testTask.doTask();
            System.out.println();
            getLogger().info("result:{}", JsonUtils.bean2Json(result));
            System.out.println();

        } catch (Exception e) {
            getLogger().error("doTask error", e);
        }
        getLogger().info("{} cost {}(ms)", getTaskName(), (System.currentTimeMillis() - startTime));
        prettyEnd();
        return result;
    }

    String getTaskName() {
        return "task";
    }

    void prettyStart() {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < lenSharp(); i++) {
            line.append("#");
        }
        StringBuilder line1 = new StringBuilder(line);
        for (int i = 0; i < "  START  ".length(); i++) {
            line1.append("#");
        }
        String line2 = line.insert(lenSharp() / 2, "  START  ").toString();
        System.out.println(line1.toString());
        System.out.println(line2);
        System.out.println("");
    }

    void prettyEnd() {
        getLogger().info("");
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < lenSharp(); i++) {
            line.append("#");
        }
        StringBuilder line2 = new StringBuilder(line);
        for (int i = 0; i < "   END   ".length(); i++) {
            line2.append("#");
        }
        StringBuilder line1 = line.insert(lenSharp() / 2, "   END   ");
        System.out.println(line.toString());
        System.out.println(line2.toString());
    }

    //#号的个数
    int lenSharp() {
        return 80;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
