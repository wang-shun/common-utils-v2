package com.youzan.sz.common.redis;

import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;


/**
 * Created by YANG on 16/4/11.
 */
public class JedisUtils {
    
    private static final String OK_CODE = "OK";
    
    private static final String OK_MULTI_CODE = "+OK";
    
    private static final String NIL = "nil";
    
    
    public static void destroyJedis(Jedis jedis) {
        if (jedis != null && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception ignored) {
                }
                
                jedis.disconnect();
            } catch (Exception ignored) {
            }
        }
        
    }
    
    
    public static boolean isNil(String nil) {
        return nil == null || "".equals(nil) || NIL.equals(nil);
    }
    
    
    public static boolean isStatusOk(String status) {
        return status != null && (OK_CODE.equals(status) || OK_MULTI_CODE.equals(status));
    }
    
    
    public static boolean ping(JedisPool pool) {
        JedisTemplate template = new JedisTemplate(pool);
        try {
            String e = template.execute(BinaryJedis::ping);
            return e != null && e.equals("PONG");
        } catch (JedisException je) {
            return false;
        }
    }
}
