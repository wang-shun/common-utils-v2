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
            LOGGER.error("Jedis Exception:{}", je);
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
            LOGGER.error("Jedis Exception:{}", je);
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
            LOGGER.error("Jedis Exception:{}", je);
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
            LOGGER.error("Jedis Exception:{}", je);
            throw new BusinessException((long) ResponseCode.REDIS_ERROR.getCode(), je.getMessage());
        } finally {
            closeResource(jedis);
        }
    }


    public boolean del(final String... keys) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.del(keys) > 0);
    }

    public String get(final String key) {
        return this.execute((JedisAction<String>) jedis -> jedis.get(key));
    }

    public Long getAsLong(final String key) {
        return this.execute((JedisAction<Long>) jedis -> {
                    String result = jedis.get(key);
                    return result != null ? Long.valueOf(result) : null;
                }
        );
    }

    public Integer getAsInteger(final String key) {
        return this.execute((JedisAction<Integer>) jedis -> {
                    String result = jedis.get(key);
                    return result != null ? Integer.valueOf(result) : null;
                }
        );
    }

    public Boolean set(final String key, final String value) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.set(key, value)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setex(final String key, final String value, final int seconds) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.setex(key, seconds, value)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setnx(final String key, final String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.setnx(key, value).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean setnxex(final String key, final String value, final long seconds) {
        return this.execute((JedisAction<Boolean>) jedis -> {
            String result = jedis.set(key, value, "NX", "EX", seconds);
            return JedisUtils.isStatusOk(result) ? Boolean.TRUE : Boolean.FALSE;
        });
    }

    public String getSet(final String key, final String value) {
        return this.execute((JedisAction<String>) jedis -> jedis.getSet(key, value));
    }

    public Long incr(final String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.incr(key));
    }

    public Long incrBy(final String key, final Long increment) {
        return this.execute((JedisAction<Long>) jedis -> jedis.incrBy(key, increment.longValue()));
    }

    public Double incrByFloat(final String key, final double increment) {
        return this.execute((JedisAction<Double>) jedis -> jedis.incrByFloat(key, increment));
    }

    public Long decr(final String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.decr(key));
    }

    public Long decrBy(final String key, final long decrement) {
        return this.execute((JedisAction<Long>) jedis -> jedis.decrBy(key, decrement));
    }

    public String hget(final String key, final String field) {
        return this.execute((JedisAction<String>) jedis -> jedis.hget(key, field));
    }

    public List<String> hmget(final String key, final String... fields) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.hmget(key, fields));
    }

    public Map<String, String> hgetAll(final String key) {
        return this.execute((JedisAction<Map<String, String>>) jedis -> jedis.hgetAll(key));
    }

    public void hset(final String key, final String field, final String value) {
        this.execute((JedisActionNoResult) jedis -> jedis.hset(key, field, value));
    }

    public Boolean hmset(final String key, final Map<String, String> map) {
        return this.execute((JedisAction<Boolean>) jedis -> JedisUtils.isStatusOk(jedis.hmset(key, map)) ? Boolean.TRUE : Boolean.FALSE);
    }

    public void hsetnx(final String key, final String field, final String value) {
        this.execute((JedisActionNoResult) jedis -> jedis.hsetnx(key, field, value));
    }

    public Long hincrBy(final String key, final String field, final long increment) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hincrBy(key, field, increment));
    }

    public Double hincrByFloat(final String key, final String field, final double increment) {
        return this.execute((JedisAction<Double>) jedis -> jedis.hincrByFloat(key, field, increment));
    }

    public Long hdel(final String key, final String... fields) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hdel(key, fields));
    }

    public Boolean hexists(final String key, final String field) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.hexists(key, field));
    }

    public Set<String> hkeys(final String key) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.hkeys(key));
    }

    public Long hlen(final String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.hlen(key));
    }

    public Long lpush(final String key, final String... values) {
        return this.execute((JedisAction<Long>) jedis -> jedis.lpushx(key, values));
    }

    public String rpop(final String key) {
        return this.execute((JedisAction<String>) jedis -> jedis.rpop(key));
    }

    /**
     * @Deprecated unusable command, this command will be removed in 3.0.0.
     */
    @Deprecated
    public String brpop(final String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.brpop(key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public String brpop(final int timeout, final String key) {
        return this.execute((JedisAction<String>) jedis -> {
            List result = jedis.brpop(timeout, key);
            return result != null && result.size() > 0 ? (String) result.get(0) : null;
        });
    }

    public Long llen(final String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.llen(key));
    }

    public String lindex(final String key, final long index) {
        return this.execute((JedisAction<String>) jedis -> jedis.lindex(key, index));
    }

    public List<String> lrange(final String key, final int start, final int end) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.lrange(key, (long) start, (long) end));
    }

    public void ltrim(final String key, final int start, final int end) {
        this.execute((JedisActionNoResult) jedis -> jedis.ltrim(key, (long) start, (long) end));
    }

    public void ltrimFromLeft(final String key, final int size) {
        this.execute((JedisActionNoResult) jedis -> jedis.ltrim(key, 0L, (long) (size - 1)));
    }

    public Boolean lremFirst(final String key, final String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.lrem(key, 1L, value).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean lremAll(final String key, final String value) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.lrem(key, 0L, value) > 0L ? Boolean.TRUE : Boolean.FALSE);
    }

    public Boolean sadd(final String key, final String... members) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.sadd(key, members).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Set<String> smembers(final String key) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.smembers(key));
    }

    public Boolean zadd(final String key, final double score, final String member) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.zadd(key, score, member).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Double zscore(final String key, final String member) {
        return this.execute((JedisAction<Double>) jedis -> jedis.zscore(key, member));
    }

    public Long zrank(final String key, final String member) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zrank(key, member));
    }

    public Long zrevrank(final String key, final String member) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zrevrank(key, member));
    }

    public Long zcount(final String key, final double min, final double max) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zcount(key, min, max));
    }

    public Set<String> zrange(final String key, final int start, final int end) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrange(key, (long) start, (long) end));
    }

    public Set<Tuple> zrangeWithScores(final String key, final int start, final int end) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrangeWithScores(key, (long) start, (long) end));
    }

    public Set<String> zrevrange(final String key, final int start, final int end) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrevrange(key, (long) start, (long) end));
    }

    public Set<Tuple> zrevrangeWithScores(final String key, final int start, final int end) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrevrangeWithScores(key, (long) start, (long) end));
    }

    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrangeByScore(key, min, max));
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrangeByScoreWithScores(key, min, max));
    }

    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        return this.execute((JedisAction<Set<String>>) jedis -> jedis.zrevrangeByScore(key, min, max));
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        return this.execute((JedisAction<Set<Tuple>>) jedis -> jedis.zrevrangeByScoreWithScores(key, min, max));
    }

    public Boolean zrem(final String key, final String... members) {
        return this.execute((JedisAction<Boolean>) jedis -> jedis.zrem(key, members).equals(SET_SUCCESS) ? Boolean.TRUE : Boolean.FALSE);
    }

    public Long zremrangeByScore(final String key, final double start, final double end) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zremrangeByScore(key, start, end));
    }

    public Long zremrangeByRank(final String key, final long start, final long end) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zremrangeByRank(key, start, end));
    }

    public Long zcard(final String key) {
        return this.execute((JedisAction<Long>) jedis -> jedis.zcard(key));
    }

    public void expire(final String key, final int seconds) {
        this.execute((JedisActionNoResult) jedis -> jedis.expire(key, seconds));
    }


    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        this.execute((JedisActionNoResult) jedis -> jedis.subscribe(jedisPubSub, channels));
    }

    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        this.execute((JedisActionNoResult) jedis -> jedis.psubscribe(jedisPubSub, patterns));
    }

    public Long publish(final String channel, final String message) {
        return this.execute((JedisAction<Long>) jedis -> jedis.publish(channel, message));
    }

    public List<String> pubsubChannels(final String pattern) {
        return this.execute((JedisAction<List<String>>) jedis -> jedis.pubsubChannels(pattern));
    }

    public Map<String, String> pubsubNumSub(final String... channels) {
        return this.execute((JedisAction<Map<String, String>>) jedis -> jedis.pubsubNumSub(channels));
    }


    public Boolean acquireLock(final String lock, final long timeout) {
        return this.execute((JedisAction<Boolean>) jedis -> {
            long expired = timeout;
            long value = System.currentTimeMillis() + expired + 1L;

            Long acquired = jedis.setnx(lock, String.valueOf(value));
            if (SET_SUCCESS.equals(acquired)) {
                return Boolean.TRUE;
            } else {
                long oldValue = Long.valueOf(jedis.get(lock)).longValue();
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

    public void releaseLock(final String lock) {
        this.execute((JedisActionNoResult) jedis -> {
            long current = System.currentTimeMillis();
            if (current < Long.valueOf(jedis.get(lock)).longValue()) {
                jedis.del(lock);
            }
        });
    }


    /**
     * 关闭资源
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
