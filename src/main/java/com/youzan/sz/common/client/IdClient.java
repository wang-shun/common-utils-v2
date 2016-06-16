package com.youzan.sz.common.client;

import redis.clients.jedis.Jedis;

/**
 * Created by zefa on 16/4/18.
 */
public class IdClient extends Jedis {
    public IdClient(String host, int port) {
        super(host, port);
    }

    public long getId(String key, String field) {
//        checkIsInMultiOrPipeline(); //TODO 临时修改,兼容jedis 2.5.2
        client.hget(key, field);
        return client.getIntegerReply();
    }
    public static void main(String[] args) throws Throwable {
        IdClient j = new IdClient("192.168.66.202", 6000);
//        System.out.println(j.getId("snowflake", "dft"));
        for(int i = 0 ; i < 100 ; i++){
            System.out.println("bug:" + j.getId("step","bug"));
            for(int k = 0 ; k < 5; k++){
                System.out.println("dft:" + j.getId("step", "dft"));
            }
        }

    }
}