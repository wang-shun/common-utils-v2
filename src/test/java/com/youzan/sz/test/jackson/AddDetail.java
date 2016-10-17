package com.youzan.sz.test.jackson;

import com.youzan.sz.common.annotation.JacksonField;

/**
 * Created by wangpan on 2016/10/17.
 */
public class AddDetail {
    @JacksonField(value = "addr_country")
    private String country;
}
