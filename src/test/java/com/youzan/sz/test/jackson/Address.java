package com.youzan.sz.test.jackson;

import com.youzan.sz.common.annotation.JacksonField;

import java.util.List;

/**
 * Created by wangpan on 2016/10/17.
 */
public class Address {

    @JacksonField(value = "reea_code")
    private int ereaCode;

    private AddDetail addDetail;

    private List<AddSimple> addSimple;
}
