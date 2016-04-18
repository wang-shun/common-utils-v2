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
        checkIsInMultiOrPipeline();
        client.hget(key, field);
        return client.getIntegerReply();
    }
    public static void main(String[] args) throws Throwable {
        IdClient j = new IdClient("192.168.66.202", 6000);
        System.out.println(j.getId("snowflake", "dft"));
        System.out.println(j.getId("step","bug"));
    }
}