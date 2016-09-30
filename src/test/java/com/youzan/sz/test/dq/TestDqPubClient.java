package com.youzan.sz.test.dq;

import com.youzan.sz.dqueue.AbstractDqPubClient;

/**
 * Created by wangpan on 2016/9/30.
 */
public class TestDqPubClient extends AbstractDqPubClient {

    public TestDqPubClient(String dequeueURL, String chanel) {
        super(dequeueURL, chanel);
    }

    public void init() {

    }
}
