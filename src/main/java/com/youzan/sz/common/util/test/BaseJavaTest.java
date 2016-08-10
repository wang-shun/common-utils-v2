package com.youzan.sz.common.util.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 测试javaAPI
 */
public class BaseJavaTest extends BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseJavaTest.class);

    public static abstract class TimeCost implements Runnable {
        private int count = 1;

        public TimeCost() {
        }

        public TimeCost(int count) {
            this.count = count;
            start();
        }

        public void start() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                run();
                LOGGER.debug("finished count:{}", i);
            }
            long cost = (System.currentTimeMillis() - start);
            String costStr;
            if (cost > 1000 && cost < 60 * 1000) {
                costStr = cost / 1000f + "(s)";
            } else if (cost < 60 * 1000 * 60) {
                costStr = cost / 1000f * 60 + "(min)";
            } else {
                costStr = cost + "(ms)";
            }

            LOGGER.info("execute count:{},time cost:{}(ms)", count, costStr);

        }
    }
}
