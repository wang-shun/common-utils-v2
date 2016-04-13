package com.youzan.sz.common.redis;

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

    public JedisUtils() {
    }

    public static boolean isStatusOk(String status) {
        return status != null && ("OK".equals(status) || "+OK".equals(status));
    }

    public static boolean isNil(String nil) {
        return nil == null || "nil".equals(nil);
    }

    public static void destroyJedis(Jedis jedis) {
        if (jedis != null && jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception var2) {
                    ;
                }

                jedis.disconnect();
            } catch (Exception var3) {
                ;
            }
        }

    }

    public static boolean ping(JedisPool pool) {
        JedisTemplate template = new JedisTemplate(pool);

        try {
            String e = (String) template.execute(new JedisTemplate.JedisAction() {
                public String action(Jedis jedis) {
                    return jedis.ping();
                }
            });
            return e != null && e.equals("PONG");
        } catch (JedisException var3) {
            return false;
        }
    }
}
