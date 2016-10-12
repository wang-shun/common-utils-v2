package com.youzan.sz.dqueue;

import com.youzan.dqueue.client.DQueue;
import com.youzan.dqueue.client.entity.Response;
import com.youzan.dqueue.client.exceptions.DQueueException;
import com.youzan.sz.dqueue.handler.LinkedDqHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangpan on 2016/9/30.
 */
public abstract class AbstractDqClient {

    private static final Logger LOGGER          = LoggerFactory.getLogger(AbstractDqClient.class);

    /**
     * client
     */
    protected DQueue dq;
    /**
     * 消息chanel
     */
    protected String chanel;

    /** handler*/
    protected LinkedDqHandler linkedDqHandler;

    public AbstractDqClient(String dequeueURL, String chanel) {
        dq = new DQueue(dequeueURL);
        this.chanel = chanel;
    }

    public String getChanel() {
        return chanel;
    }


    public LinkedDqHandler getLinkedDqHandler() {
        return linkedDqHandler;
    }

    public void setLinkedDqHandler(LinkedDqHandler linkedDqHandler) {
        this.linkedDqHandler = linkedDqHandler;
    }

    public Boolean finish(String key) throws DQueueException {

        Response response = dq.finish(key);
        if (!response.isSuccess()) {
            LOGGER.error("finish dqueue key = {},error = {}", key, response.getError());
            return false;
        }
        return true;
    }

    public Boolean delete(String key) throws DQueueException {
        Response response = dq.delete(key);
        if (!response.isSuccess()) {
            LOGGER.error("delete dqueue key = {},error = {}", key, response.getError());
            return false;
        }
        return true;
    }


}