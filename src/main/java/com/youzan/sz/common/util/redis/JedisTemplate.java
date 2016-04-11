package com.youzan.sz.common.util.redis;

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


    public <T> T execute(JedisAction<T> jedisAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            T t = jedisAction.action(jedis);
            return t;
        } catch (JedisException je) {
            LOGGER.error("Jedis Exception:{}", je);
            throw je;
        } finally {
            closeResource(jedis);
        }
    }

    public void execute(JedisActionNoResult jedisActionNoResult) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedisActionNoResult.action(jedis);
        } catch (JedisException je) {
            LOGGER.error("Jedis Exception:{}", je);
            throw je;
        } finally {
            closeResource(jedis);
        }
    }

    public List<Object> execute(PipelineAction pipelineAction) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipelineAction.action(pipeline);
            return pipeline.syncAndReturnAll();
        } catch (JedisException je) {
            LOGGER.error("Jedis Exception:{}", je);
            throw je;
        } finally {
            closeResource(jedis);
        }
    }

    public void execute(PipelineActionNoResult pipelineActionNoResult) throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipeline = jedis.pipelined();
            pipelineActionNoResult.action(pipeline);
            pipeline.sync();
        } catch (JedisException je) {
            LOGGER.error("Jedis Exception:{}", je);
            throw je;
        } finally {
            closeResource(jedis);
        }
    }


    public boolean del(final String... keys) {
        return ((Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                String[] var2 = keys;
                int var3 = var2.length;

                for (int var4 = 0; var4 < var3; ++var4) {
                    String key = var2[var4];
                    jedis.del(key);
                }

                return Boolean.valueOf(true);
            }
        })).booleanValue();
    }

    public String get(final String key) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public Long getAsLong(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                String result = jedis.get(key);
                return result != null ? Long.valueOf(result) : null;
            }
        });
    }

    public Integer getAsInteger(final String key) {
        return (Integer) this.execute(new JedisAction() {
            public Integer action(Jedis jedis) {
                String result = jedis.get(key);
                return result != null ? Integer.valueOf(result) : null;
            }
        });
    }

    public void set(final String key, final String value) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.set(key, value);
            }
        });
    }

    public void setex(final String key, final String value, final int seconds) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.setex(key, seconds, value);
            }
        });
    }

    public Boolean setnx(final String key, final String value) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.setnx(key, value).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Boolean setnxex(final String key, final String value, final long seconds) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                String result = jedis.set(key, value, "NX", "EX", seconds);
                return Boolean.valueOf(JedisUtils.isStatusOk(result));
            }
        });
    }

    public String getSet(final String key, final String value) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                return jedis.getSet(key, value);
            }
        });
    }

    public Long incr(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    public Long incrBy(final String key, final Long increment) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.incrBy(key, increment.longValue());
            }
        });
    }

    public Double incrByFloat(final String key, final double increment) {
        return (Double) this.execute(new JedisAction() {
            public Double action(Jedis jedis) {
                return jedis.incrByFloat(key, increment);
            }
        });
    }

    public Long decr(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    public Long decrBy(final String key, final long decrement) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.decrBy(key, decrement);
            }
        });
    }

    public String hget(final String key, final String field) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public List<String> hmget(final String key, final String... fields) {
        return (List) this.execute(new JedisAction() {
            public List<String> action(Jedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) {
        return (Map) this.execute(new JedisAction() {
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    public void hset(final String key, final String field, final String value) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.hset(key, field, value);
            }
        });
    }

    public void hmset(final String key, final Map<String, String> map) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.hmset(key, map);
            }
        });
    }

    public Boolean hsetnx(final String key, final String field, final String value) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.hsetnx(key, field, value).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Long hincrBy(final String key, final String field, final long increment) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.hincrBy(key, field, increment);
            }
        });
    }

    public Double hincrByFloat(final String key, final String field, final double increment) {
        return (Double) this.execute(new JedisAction() {
            public Double action(Jedis jedis) {
                return jedis.hincrByFloat(key, field, increment);
            }
        });
    }

    public Long hdel(final String key, final String... fields) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.hdel(key, fields);
            }
        });
    }

    public Boolean hexists(final String key, final String field) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.hexists(key, field);
            }
        });
    }

    public Set<String> hkeys(final String key) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.hkeys(key);
            }
        });
    }

    public Long hlen(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.hlen(key);
            }
        });
    }

    public Long lpush(final String key, final String... values) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.lpushx(key, values);
            }
        });
    }

    public String rpop(final String key) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                return jedis.rpop(key);
            }
        });
    }

    public String brpop(final String key) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                List result = jedis.brpop(key);
                return result != null && result.size() > 0 ? (String) result.get(0) : null;
            }
        });
    }

    public String brpop(final int timeout, final String key) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                List result = jedis.brpop(timeout, key);
                return result != null && result.size() > 0 ? (String) result.get(0) : null;
            }
        });
    }

    public Long llen(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }

    public String lindex(final String key, final long index) {
        return (String) this.execute(new JedisAction() {
            public String action(Jedis jedis) {
                return jedis.lindex(key, index);
            }
        });
    }

    public List<String> lrange(final String key, final int start, final int end) {
        return (List) this.execute(new JedisAction() {
            public List<String> action(Jedis jedis) {
                return jedis.lrange(key, (long) start, (long) end);
            }
        });
    }

    public void ltrim(final String key, final int start, final int end) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.ltrim(key, (long) start, (long) end);
            }
        });
    }

    public void ltrimFromLeft(final String key, final int size) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.ltrim(key, 0L, (long) (size - 1));
            }
        });
    }

    public Boolean lremFirst(final String key, final String value) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.lrem(key, 1L, value).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Boolean lremAll(final String key, final String value) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.lrem(key, 0L, value).longValue() > 0L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Boolean sadd(final String key, final String... members) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.sadd(key, members).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Set<String> smembers(final String key) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.smembers(key);
            }
        });
    }

    public Boolean zadd(final String key, final double score, final String member) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.zadd(key, score, member).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Double zscore(final String key, final String member) {
        return (Double) this.execute(new JedisAction() {
            public Double action(Jedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }

    public Long zrank(final String key, final String member) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zrank(key, member);
            }
        });
    }

    public Long zrevrank(final String key, final String member) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zrevrank(key, member);
            }
        });
    }

    public Long zcount(final String key, final double min, final double max) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zcount(key, min, max);
            }
        });
    }

    public Set<String> zrange(final String key, final int start, final int end) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.zrange(key, (long) start, (long) end);
            }
        });
    }

    public Set<Tuple> zrangeWithScores(final String key, final int start, final int end) {
        return (Set) this.execute(new JedisAction() {
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrangeWithScores(key, (long) start, (long) end);
            }
        });
    }

    public Set<String> zrevrange(final String key, final int start, final int end) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.zrevrange(key, (long) start, (long) end);
            }
        });
    }

    public Set<Tuple> zrevrangeWithScores(final String key, final int start, final int end) {
        return (Set) this.execute(new JedisAction() {
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrevrangeWithScores(key, (long) start, (long) end);
            }
        });
    }

    public Set<String> zrangeByScore(final String key, final double min, final double max) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.zrangeByScore(key, min, max);
            }
        });
    }

    public Set<Tuple> zrangeByScoreWithScores(final String key, final double min, final double max) {
        return (Set) this.execute(new JedisAction() {
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrangeByScoreWithScores(key, min, max);
            }
        });
    }

    public Set<String> zrevrangeByScore(final String key, final double max, final double min) {
        return (Set) this.execute(new JedisAction() {
            public Set<String> action(Jedis jedis) {
                return jedis.zrevrangeByScore(key, max, min);
            }
        });
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key, final double max, final double min) {
        return (Set) this.execute(new JedisAction() {
            public Set<Tuple> action(Jedis jedis) {
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
    }

    public Boolean zrem(final String key, final String... members) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                return jedis.zrem(key, members).longValue() == 1L ? Boolean.TRUE : Boolean.FALSE;
            }
        });
    }

    public Long zremrangeByScore(final String key, final double start, final double end) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zremrangeByScore(key, start, end);
            }
        });
    }

    public Long zremrangeByRank(final String key, final long start, final long end) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zremrangeByRank(key, start, end);
            }
        });
    }

    public Long zcard(final String key) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.zcard(key);
            }
        });
    }

    public void expire(final String key, final int seconds) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.expire(key, seconds);
            }
        });
    }


    public void subscribe(final JedisPubSub jedisPubSub, final String... channels) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.subscribe(jedisPubSub, channels);
            }
        });
    }

    public void psubscribe(final JedisPubSub jedisPubSub, final String... patterns) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                jedis.psubscribe(jedisPubSub, patterns);
            }
        });
    }

    public Long publish(final String channel, final String message) {
        return (Long) this.execute(new JedisAction() {
            public Long action(Jedis jedis) {
                return jedis.publish(channel, message);
            }
        });
    }

    public List<String> pubsubChannels(final String pattern) {
        return (List) this.execute(new JedisAction() {
            public List<String> action(Jedis jedis) {
                return jedis.pubsubChannels(pattern);
            }
        });
    }

    public Map<String, String> pubsubNumSub(final String... channels) {
        return (Map) this.execute(new JedisAction() {
            public Map<String, String> action(Jedis jedis) {
                return jedis.pubsubNumSub(channels);
            }
        });
    }

    public Boolean acquireLock(final String lock, final long timeout) {
        return (Boolean) this.execute(new JedisAction() {
            public Boolean action(Jedis jedis) {
                boolean success = false;
                long expired = timeout;
                long value = System.currentTimeMillis() + expired + 1L;
                System.out.println(value);
                long acquired = jedis.setnx(lock, String.valueOf(value)).longValue();
                if (acquired == 1L) {
                    success = true;
                } else {
                    long oldValue = Long.valueOf(jedis.get(lock)).longValue();
                    if (oldValue < System.currentTimeMillis()) {
                        String getValue = jedis.getSet(lock, String.valueOf(value));
                        if (Long.valueOf(getValue).longValue() == oldValue) {
                            success = true;
                        } else {
                            success = false;
                        }
                    } else {
                        success = false;
                    }
                }

                return Boolean.valueOf(success);
            }
        });
    }

    public void releaseLock(final String lock) {
        this.execute(new JedisActionNoResult() {
            public void action(Jedis jedis) {
                long current = System.currentTimeMillis();
                if (current < Long.valueOf(jedis.get(lock)).longValue()) {
                    jedis.del(lock);
                }

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
