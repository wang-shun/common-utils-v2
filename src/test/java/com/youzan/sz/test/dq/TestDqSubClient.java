package com.youzan.sz.test.dq;

import com.youzan.sz.dqueue.AbstractDqSubClent;

/**
 * Created by wangpan on 2016/9/30.
 */
public class TestDqSubClient extends AbstractDqSubClent {
    public TestDqSubClient(String dequeueURL, String chanel, Class z) {
        super(dequeueURL, chanel, z);
    }

    @Override
    public void init() {
       // popAlways();
        try {
            System.in.read();
        }catch (Exception  e){

        }
    }
}
