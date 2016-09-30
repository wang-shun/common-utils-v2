package com.youzan.sz.test.dq;

import com.youzan.sz.dqueue.AbstractDqPubClient;

/**
 * Created by wangpan on 2016/9/30.
 */
public class TestNqPubClient extends AbstractDqPubClient {

    public TestNqPubClient(String dequeueURL, String chanel) {
        super(dequeueURL, chanel);
    }

    @Override
    public void init() {

    }
}
