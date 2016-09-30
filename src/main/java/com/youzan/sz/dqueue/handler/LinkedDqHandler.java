package com.youzan.sz.dqueue.handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangpan on 2016/9/30.
 */
public class LinkedDqHandler {

    private List<DqHandler> dqHandlers;

    public List<DqHandler> getDqHandlers() {
        return dqHandlers;
    }

    public void setDqHandlers(List<DqHandler> dqHandlers) {
        this.dqHandlers = dqHandlers;
    }

    public void addQqHandler(DqHandler dqHandler) {
        if (dqHandlers == null) {
            dqHandlers = new ArrayList<>();
        }
        dqHandlers.add(dqHandler);
    }

    public <T, V> T handler(String key,V v) throws Exception {
        if (dqHandlers == null || dqHandlers.size() == 0) {
            return (T) v;
        }
        Object r = v;
        for (DqHandler dqHandler : dqHandlers) {

            r = dqHandler.handler(key,r);
        }
        return (T) r;
    }
}
