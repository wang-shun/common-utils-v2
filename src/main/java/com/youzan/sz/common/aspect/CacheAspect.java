package com.youzan.sz.common.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.youzan.sz.common.annotation.CacheEvict;
import com.youzan.sz.common.annotation.Cacheable;
import com.youzan.sz.common.redis.JedisTemplate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by YANG on 16/4/13.
 */

@Aspect
public class CacheAspect extends BaseAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheAspect.class);

    @Value("${CacheEnable}")
    private boolean cacheEnable;

    @Resource
    private JedisTemplate jedisTemplate;


    @Around("@annotation(com.youzan.sz.common.annotation.Cacheable)")
    public Object cache(ProceedingJoinPoint pjp) {
        Object result = null;
        if (!cacheEnable) {
            try {
                result = pjp.proceed();
            } catch (Throwable throwable) {
                LOGGER.error("Cache Exceptio:{}", throwable);
            }

            return result;
        } else {
            Method method = super.getMethod(pjp);
            Cacheable cacheable = method.getAnnotation(Cacheable.class);
            String field = (String) super.parseKey(cacheable.field(), method, pjp.getArgs());
            Class returnType = ((MethodSignature) pjp.getSignature()).getReturnType();

            String strResult = null;

            if (field != null) {
                strResult = jedisTemplate.hget(cacheable.key(), field);
            }

            if (strResult != null && !strResult.equals("nil")) {
                // 处理泛型
                Type returnType2 = method.getGenericReturnType();
                if (returnType2 instanceof ParameterizedType) {
                    result = JSON.parseObject(strResult, returnType2, new Feature[0]);
                } else {
                    result = JSON.parseObject(strResult, returnType);
                }
            } else {
                //缓存未找到的情况
                try {
                    result = pjp.proceed();
                    if (result != null) {
                        jedisTemplate.hset(cacheable.key(), field, JSON.toJSONString(result));
                        jedisTemplate.expire(cacheable.key(), cacheable.expireTime());
                    }
                } catch (Throwable throwable) {
                    LOGGER.error("Cache Exceptio:{}", throwable);
                }
            }
            return result;
        }
    }

    @Around("@annotation(com.youzan.sz.common.annotation.CacheEvict)")
    public Object evict(ProceedingJoinPoint pjp) {
        if (cacheEnable) {
            Method method = getMethod(pjp);
            CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);
            String field = (String) parseKey(cacheEvict.field(), method, pjp.getArgs());
            jedisTemplate.hdel(cacheEvict.key(), field);
        }
        try {
            return pjp.proceed();
        } catch (Throwable throwable) {
            LOGGER.error("Cache Exception:{}", throwable);
        }
        return null;
    }


}
