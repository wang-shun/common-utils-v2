package com.youzan.sz.dqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.dqueue.client.entity.Job;
import com.youzan.dqueue.client.entity.Response;
import com.youzan.dqueue.client.exceptions.DQueueException;
import com.youzan.sz.dqueue.codec.Encode;
import com.youzan.sz.dqueue.codec.json.StringEncode;

/**
 * Created by wangpan on 2016/9/30.
 */
public abstract class AbstractDqPubClient extends AbstractDqClient {

    private static final Logger LOGGER          = LoggerFactory.getLogger(AbstractDqPubClient.class);
    /**delay time,单位s*/
    private int                 interval        = 30;
    /**被消费消费但没响应的超时时间*/
    private int                 reservedTimeout = 30;
    /**编码*/
    private Encode              encode          = new StringEncode();

    public AbstractDqPubClient(String dequeueURL, String chanel) {
        super(dequeueURL, chanel);
    }

    public Encode getEncode() {
        return encode;
    }

    public void setEncode(Encode encode) {
        this.encode = encode;
    }

    public int getReservedTimeout() {
        return reservedTimeout;
    }

    public void setReservedTimeout(int reservedTimeout) {
        this.reservedTimeout = reservedTimeout;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public Boolean pub(String key, Object object) throws DQueueException,Exception {
        Job job = new Job();
        job.setChannel(this.getChanel());
        job.setInterval(this.getInterval());
        job.setReservedTimeout(this.getReservedTimeout());
        job.setKey(key);
        Object msg = null;
        if (linkedDqHandler != null) {
            msg = linkedDqHandler.handler(key,object);
        }else {
            msg = object;
        }

        if (!(msg instanceof String)) {
            job.setValue(encode.encode(msg));
        }else {
            job.setValue((String)msg);
        }

        Response re = dq.add(job);
        if (!re.isSuccess()) {
            LOGGER.error("pub dqueue key = {},msg = {}, error = {}", job.getKey(), object, re.getError());
            return false;
        }
        LOGGER.debug("pub dq msg={} success",object);
        return true;
    }
}
