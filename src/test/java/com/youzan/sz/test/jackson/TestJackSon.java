package com.youzan.sz.test.jackson;

import com.youzan.sz.common.util.JacksonFieldUtil;

import org.junit.Test;

import java.util.Map;

/**
 * Created by wangpan on 2016/10/17.
 */
public class TestJackSon {
    @Test
    public void test(){
        Map<String, String> map = JacksonFieldUtil.getJsonFiledsValue(People.class);
        System.out.println(map);
    }
}
