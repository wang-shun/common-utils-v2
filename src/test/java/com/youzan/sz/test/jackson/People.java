package com.youzan.sz.test.jackson;

import com.youzan.sz.common.annotation.JacksonField;

/**
 * Created by wangpan on 2016/10/17.
 */
public class People {
    @JacksonField(value = "reel_age")
    private int realAge;
    private Address address;
    private String name;
}
