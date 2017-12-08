package com.youzan.sz.common.client;

import com.youzan.paas.march.sdk.March;
import com.youzan.paas.march.sdk.MarchConfig;
import com.youzan.paas.march.sdk.RedisBackend;
import com.youzan.paas.march.sdk.exceptions.MarchClientException;

import redis.clients.jedis.Jedis;

/**
 * Created by zefa on 16/4/18.
 */
public class IdClient{
    
    private MarchConfig config;
    
    public IdClient(String hosts, String userName, String password) {
        config = new MarchConfig(hosts.split(","));
        config.setUser(userName);
        config.setPassword(password);
    }

    public long getId(String key) throws MarchClientException {
        RedisBackend backend = March.newBackend(config);
        March march = March.newCachedClient(key, backend, 100);
        return march.next();
    }
    public static void main(String[] args) throws Throwable {
        IdClient j = new IdClient("10.9.193.20:7143,10.9.107.207:7143", "cashier", "L5VlGJj9CsycdcSVFtIk");
//        System.out.println(j.getId("snowflake", "dft"));
        for(int i = 0 ; i < 100 ; i++){
            System.out.println("bug:" + j.getId("cashier.product_id"));
            for(int k = 0 ; k < 5; k++){
                System.out.println("dft:" + j.getId("cashier.product_id"));
            }
        }

    }
}