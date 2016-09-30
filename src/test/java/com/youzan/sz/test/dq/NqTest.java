package com.youzan.sz.test.dq;

import com.youzan.dqueue.client.exceptions.DQueueException;
import com.youzan.sz.dqueue.codec.json.StringDecode;
import com.youzan.sz.dqueue.handler.DqHandler;
import com.youzan.sz.dqueue.handler.LinkedDqHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangpan on 2016/9/30.
 */
public class NqTest {
    private static final String DQUEUE_URI = "http://delay-dev.s.qima-inc.com:80/json";
    private static final String CHANEL = "wp_1";
    private static final Logger LOGGER = LoggerFactory.getLogger(NqTest.class);
    @Test
    public void pub(){

        TestNqPubClient client =  new TestNqPubClient(DQUEUE_URI,CHANEL);
        DemoPoeple demoPoeple = new DemoPoeple();

        try{
            int i  =0;
            while(true) {
                client.pub(String.valueOf(i),demoPoeple);
                Thread.sleep(3000);
                // break;
                i++;
                demoPoeple.setAge(i);
            }
        }catch (DQueueException e){
            LOGGER.error("error {}",e);
        }catch (Exception e){

        }

    }
    @Test
    public void sub(){

      TestNqSubClient client = new TestNqSubClient(DQUEUE_URI,CHANEL,DemoPoeple.class);
        DqHandler handler = new DqHandler() {
            @Override
            public <T, V> T handler(String key,V v)  {
                try {
                    LOGGER.info("receive msg key={},v={}", key, v);
                    client.delete(key);
                }catch (Exception e){

                }
                return null;
            }
        };
        LinkedDqHandler linkedDqHandler = new LinkedDqHandler();
        linkedDqHandler.addQqHandler(handler);
        client.setLinkedDqHandler(linkedDqHandler);
        client.setAutoDelete(true);
        client.setDecode(new StringDecode());
        client.init();

    }
}
