package com.youzan.sz.common.redis;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by YANG on 16/4/11.
 */


public class JedisTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisTemplate.class);

    private static final Long SET_SUCCESS = 1L;

    private JedisPool jedisPool;

    public JedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    public <T> T execute(JedisAction<T> jedisAction) throws BusinessException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedisAction.action(jedis);
        } catch (JedisException je) {
            throw new BusinessException((long) ResponseCode.REDIS_ERROR.getCode(), je.getMessage());
        } finally {
            closeResource(jedis);
        }
    }

    public void execute(JedisActionNoResult jedisActionNoResult) throws BusinessException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedisActionNoResult.action(jedis);
        } catch (JedisException je) {
            throw new BusinessException((long) ResponseCode.REDIS_ERROR.getCode(), je.getMessage());
        } finally {
            closeResource(jedis);
        }
    }

    public List<Object> execute(PipelineAction pipelineAction) throws BusinessException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipelineAction.action(pipeline);
            return pipeline.syncAndReturnAll();
        } catch (JedisException je) {
            throw new BusinessException((long) ResponseCode.REDIS_ERROR.getCode(), je.getMessage());
        } finally {
            closeResource(jedis);
        }
    }

    public void execute(PipelineActionNoResult pipelineActionNoResult) throws BusinessException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipelineActionNoResult.action(pipeline);
            pipeline.sync();
        } catch (JedisException je) {
            throw new BusinessException((long) ResponseCode.REDIS_ERROR.getCode(), je.getMessage());
        } finally {
            closeResource(jedis);
        }
    }


    public boolean del(String... keys) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.del(keys) > 0);
    }

    public String get(String key) {
        return this.execute((JedisAction<String>) jedis -> jedis.get(key));
    }

    public Long getAsLong(String key) {
        return this.execute((JedisAction<Long>) jedis -> {
                    String result = jedis.get(key);
                    return result != null ? Long.valueOf(result) : null;
                }
        );
    }

    public Integer getAsInteger(String key) {
        return this.execute((JedisAction<Integer>) jedis -> {
                    String result = jedis.get(key);
                    return result != null ? Integer.valueOf(result) : null;
                }
        );
    }

    public Boolean set(String key, String value) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.set(key, value)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setex(String key, String value, int seconds) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.setex(key, seconds, value)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setnx(String key, String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.setnx(key, value).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setnxex(String key, String value, long seconds) {
        return this.execute((JedisAction<Boolean>) jedis -> {
            String result = jedis.set(key, value, "NX", "EX", seconds);
            return JedisUtils.isStatusOk(result) ? Boolean.TRUE : Boolean.FALSE;
        });
    }

    public String getSet(String key, String value) {
        return this.execute((JedisAction<String>) jedis -> jedis.getSet(key, value));
    }

    public Long incr(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.incr(key));
    }

    public Long incrBy(String key, Long increment) {
        return this.execute((JedisAction<Long>) jedis -> jedis.incrBy(key, increment.longValue()));
    }

    public Double incrByFloat(String key, double increment) {
        return this.execute((JedisAction<Double>) jedis -> jedis.incrByFloat(key, increment));
    }

    public Long decr(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.decr(key));
    }

    public Long decrBy(String key, long decrement) {
        return this.execute((JedisAction<Long>) jedis -> jedis.decrBy(key, decrement));
    }

    public String hget(String key, String field) {
        return this.execute((JedisAction<String>) jedis -> jedis.hget(key, field));
    }

    public List<String> hmget(String key, String... fields) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.hmget(key, fields));
    }

    public Map<String, String> hgetAll(String key) {
        return this.execute((JedisAction<Map<String, String>>) jedis -> jedis.hgetAll(key));
    }

    public void hset(String key, String field, String value) {
        this.execute((JedisActionNoResult) jedis -> jedis.hset(key, field, value));
    }

    public Boolean hmset(String key, Map<String, String> map) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.hmset(key, map)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public void hsetnx(String key, String field, String value) {
        this.execute((JedisActionNoResult) jedis -> jedis.hsetnx(key, field, value));
    }

    public Long hincrBy(String key, String field, long increment) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hincrBy(key, field, increment));
    }

    public Double hincrByFloat(String key, String field, double increment) {
        return this.execute((JedisAction<Double>) jedis -> jedis.hincrByFloat(key, field, increment));
    }

    public Long hdel(String key, String... fields) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hdel(key, fields));
    }

    public Boolean hexists(String key, String field) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.hexists(key, field));
    }

    public Set<String> hkeys(String key) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.hkeys(key));
    }

    public Long hlen(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hlen(key));
    }

    public Long lpush(String key, String... values) {
        return this.execute((JedisAction<Long>) jedis -> jedis.lpush(key, values));
    }

    public String lpop(String key) {
        return this.execute((JedisAction<String>) jedis -> jedis.lpop(key));
    }

    public String blpop(String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.blpop(key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public String blpop(int timeout, String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.blpop(timeout, key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public Long rpush(String key, String... values) {
        return this.execute((JedisAction<Long>) jedis -> jedis.rpush(key, values));
    }

    public String rpop(String key) {
        return this.execute((JedisAction<String>) jedis -> jedis.rpop(key));
    }


    public String brpop(String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.brpop(key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public String brpop(int timeout, String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.brpop(timeout, key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public Long llen(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.llen(key));
    }

    public String lindex(String key, long index) {
        return this.execute((JedisAction<String>) jedis -> jedis.lindex(key, index));
    }

    public List<String> lrange(String key, int start, int end) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.lrange(key, (long) start, (long) end));
    }

    public void ltrim(String key, int start, int end) {
        this.execute((JedisActionNoResult) jedis -> jedis.ltrim(key, (long) start, (long) end));
    }

    public void ltrimFromLeft(String key, int size) {
        this.execute((JedisActionNoResult) jedis -> jedis.ltrim(key, 0L, (long) (size - 1)));
    }

    public Boolean lremFirst(String key, String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.lrem(key, 1L, value).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean lremAll(String key, String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.lrem(key, 0L, value) > 0L ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean sadd(String key, String... members) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.sadd(key, members).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Set<String> smembers(String key) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.smembers(key));
    }

    public List<String> srandmember(String key, int count) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.srandmember(key, count));
    }

    public Long scard(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.scard(key));
    }

    public Long srem(String key, String... memebers) {
        return this.execute((JedisAction<Long>) jedis -> jedis.srem(key, memebers));
    }

    public Boolean zadd(String key, double score, String member) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.zadd(key, score, member).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Double zscore(String key, String member) {
        return this.execute((JedisAction<Double>) jedis -> jedis.zscore(key, member));
    }

    public Long zrank(String key, String member) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zrank(key, member));
    }

    public Long zrevrank(String key, String member) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zrevrank(key, member));
    }

    public Long zcount(String key, double min, double max) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zcount(key, min, max));
    }

    public Set<String> zrange(String key, int start, int end) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrange(key, (long) start, (long) end));
    }

    public Set<Tuple> zrangeWithScores(String key, int start, int end) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrangeWithScores(key, (long) start, (long) end));
    }

    public Set<String> zrevrange(String key, int start, int end) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrevrange(key, (long) start, (long) end));
    }

    public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrevrangeWithScores(key, (long) start, (long) end));
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrangeByScore(key, min, max));
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    public Set<String> zrevrangeByScore(String key, double max, double min) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrevrangeByScore(key, min, max));
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrevrangeByScoreWithScores(key, min, max));
    }

    public Boolean zrem(String key, String... members) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.zrem(key, members).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Long zremrangeByScore(String key, double start, double end) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zremrangeByScore(key, start, end));
    }

    public Long zremrangeByRank(String key, long start, long end) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zremrangeByRank(key, start, end));
    }

    public Long zcard(String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zcard(key));
    }

    public void expire(String key, int seconds) {
        this.execute((JedisActionNoResult) jedis -> jedis.expire(key, seconds));
    }


    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        this.execute((JedisActionNoResult) jedis -> jedis.subscribe(jedisPubSub, channels));
    }

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        this.execute((JedisActionNoResult) jedis -> jedis.psubscribe(jedisPubSub, patterns));
    }

    public Long publish(String channel, String message) {
        return this.execute((JedisAction<Long>) jedis -> jedis.publish(channel, message));
    }

    public List<String> pubsubChannels(String pattern) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.pubsubChannels(pattern));
    }

    public Map<String, String> pubsubNumSub(String... channels) {
        return this.execute((JedisAction<Map<String, String>>) jedis -> jedis.pubsubNumSub(channels));
    }


    public Boolean acquireLock(String lock, long timeout) {
        return this.execute((JedisAction<Boolean>) jedis -> {
            long value = System.currentTimeMillis() + timeout + 1L;

            Long acquired = jedis.setnx(lock, String.valueOf(value));
            if (SET_SUCCESS.equals(acquired)) {
                return Boolean.TRUE;
            } else {
                long oldValue = Long.valueOf(jedis.get(lock));
                if (oldValue < System.currentTimeMillis()) {
                    String getValue = jedis.getSet(lock, String.valueOf(value));
                    if (Long.valueOf(getValue) == oldValue) {
                        return Boolean.TRUE;
                    } else {
                        return Boolean.FALSE;
                    }
                } else {
                    return Boolean.FALSE;
                }
            }
        });
    }

    public void releaseLock(String lock) {
        this.execute((JedisActionNoResult) jedis -> {
            long current = System.currentTimeMillis();
            if (current < Long.valueOf(jedis.get(lock))) {
                jedis.del(lock);
            }
        });
    }


    /**
     * 关闭资源`
     *
     * @param jedis
     */
    public void closeResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public interface PipelineAction<T> {
        T action(Pipeline pipeline);
    }

    public interface PipelineActionNoResult {
        void action(Pipeline pipeline);
    }

    public interface JedisAction<T> {
        T action(Jedis jedis);
    }

    public interface JedisActionNoResult {
        void action(Jedis jedis);
    }
}
