package com.youzan.sz.common.util.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youzan.sz.common.util.JsonUtils;
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
    private static final ObjectMapper om = new ObjectMapper();

//    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class JsonBeanTest {
        private String t1 = null;
        private String t2 = "fsdfs";
        private String t3 = "123";

        public String getT1() {
            return t1;
        }

        public void setT1(String t1) {
            this.t1 = t1;
        }

        public String getT2() {
            return t2;
        }

        public void setT2(String t2) {
            this.t2 = t2;
        }

        public String getT3() {
            return t3;
        }

        public void setT3(String t3) {
            this.t3 = t3;
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        final String s = JsonUtils.bean2Json(new JsonBeanTest());
        final String s1 = om.writeValueAsString(new JsonBeanTest());
        System.err.println(s);
        System.err.println(s1);

    }
}
