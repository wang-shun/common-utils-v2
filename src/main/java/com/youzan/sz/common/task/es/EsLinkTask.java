package com.youzan.sz.common.task.es;

import com.youzan.sz.common.client.EsClient;
import com.youzan.sz.common.model.Page;
import com.youzan.sz.common.search.SearchItem;
import com.youzan.sz.common.search.Searchable;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

/**
 * Created by zefa on 16/5/13.
 */
@Component
public class EsLinkTask extends TimerTask {


    private static final Logger LOGGER = LoggerFactory.getLogger(EsLinkTask.class);
    @Override
    public void run() {
        LOGGER.debug("ES保持链接:" + new DateTime().toString("MM/dd/yyyy hh:mm:ss.SSSa"));
        try {
            Searchable searchable = new Searchable();
            searchable.addAnd(SearchItem.eq("status", "0"));
            searchable.addAnd(SearchItem.like("yzAccount", "1"));
            Page page = EsClient.search("shop_staff_v1", searchable);
        }catch (Exception e){
            LOGGER.error("ES链接异常" + new DateTime().toString("MM/dd/yyyy hh:mm:ss.SSSa") + "e : " + e.getMessage());
        }
    }
}
