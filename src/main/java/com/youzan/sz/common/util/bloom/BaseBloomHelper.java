package com.youzan.sz.common.util.bloom;

import static com.youzan.sz.common.util.TimeCostWrapper.doTask;

import java.util.concurrent.TimeUnit;

import com.youzan.sz.jutil.bytes.BitBuffer;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.Common;
import com.youzan.sz.common.model.base.BaseHelper;
import com.youzan.sz.common.redis.JedisTemplate;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.jutil.bloomfilter.BloomFilter;

/**
 *
 * Created by zhanguo on 2016/9/23.
 */
public abstract class BaseBloomHelper<T> extends BaseHelper {
    @Autowired
    protected JedisTemplate      jedisTemplate;
    private final static Integer NUM_ELEMENTS              = 1_000_000;
    private final static double  MAX_FALSE_POS_PROBABILITY = 0.01;
    private final static Integer PER_LOAD                  = 1000;
    protected BloomFilter        bloomFilter               = null;

    public static class BloomConfig {

        private String  bloomTopic;
        private double  probability   = MAX_FALSE_POS_PROBABILITY;
        private Integer numOfElements = NUM_ELEMENTS;
        private Integer numOfPerLoad  = PER_LOAD;

        public BloomConfig(String bloomTopic) {
            this.bloomTopic = bloomTopic;
        }

        public String getBloomTopic() {
            return bloomTopic;
        }

        public BloomConfig setBloomTopic(String bloomTopic) {
            this.bloomTopic = bloomTopic;
            return this;
        }

        public double getProbability() {
            return probability;
        }

        public BloomConfig setProbability(double probability) {
            this.probability = probability;
            return this;
        }

        public Integer getNumOfElements() {
            return numOfElements;
        }

        public BloomConfig setNumOfElements(Integer numOfElements) {
            this.numOfElements = numOfElements;
            return this;
        }

        public Integer getNumOfPerLoad() {
            return numOfPerLoad;
        }

        /**一定要id跳跃增量
         * 否则会造成无法读取数据
         * */
        public BloomConfig setNumOfPerLoad(Integer numOfPerLoad) {
            if (numOfPerLoad < 1) {
                throw ResponseCode.PARAMETER_ERROR
                    .getBusinessException("numOfPerLoad" + numOfPerLoad + " must great 1");
            }
            this.numOfPerLoad = numOfPerLoad;
            return this;
        }

        public String getRefreshMutex() {
            return getBaseKey() + "mutex";
        }

        public String getCacheCursor() {
            return getBaseKey() + "cursor";
        }

        public String getCacheKey() {
            return getBaseKey() + "cache";
        }

        public String getLoadDBTimeKey() {
            return getBaseKey() + "load_time";
        }

        private String getBaseKey() {
            return Common.APPNAME + "_bloom_" + bloomTopic + "_";
        }

    }

    public abstract BloomConfig getConfig();

    /**
     * 检查redis是否过期->没过期则从数据库拉取
     *
     * */
    public Boolean load() {
        //这里只标示上次加载时间,数据考虑到网络流量暂存本地
        logger.info("load bloom filter with config:{}", JsonUtils.toJson(getConfig()));
        logger.info("load bloom filter as consumer ");
        this.bloomFilter = loadFromCache();
        return true;
    }

    protected BloomFilter loadFromCache() {
        BloomFilter filter = BloomFilter.getFilter(NUM_ELEMENTS, MAX_FALSE_POS_PROBABILITY);

        final String bloomCacheKey = getConfig().getCacheKey();
        String bloomCache;
        int maxTry = 3;//最大尝试5*3/15分钟
        while ((bloomCache = doTask(() -> jedisTemplate.get(bloomCacheKey), 60)) == null && maxTry > 0) {
            try {
                logger.info("load filter from cache try:{}", maxTry);
                maxTry--;
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {//暂时不做收敛,如果错误较多,按道理sleep时间会越来越长
                logger.error("load bloom cache from redis error", e);
            }
        }
        if (StringUtil.isNotEmpty(bloomCache)) {
            //            final BitBuffer bitBuffer = new BitBuffer().initFromBytes(Base64.decodeBase64(bloomCache));
            final BitBuffer bitBuffer = new BitBuffer(Base64.decodeBase64(bloomCache),
                filter.getBitBuffer().getCapacity());
            filter = new BloomFilter(filter.getHashCount(), bitBuffer);
            logger.info("load bloom filter({}) from cache,hashCount:{},byte len:{}", getConfig().getBloomTopic(),
                bloomCache.length(), filter.getHashCount());
            return filter;
        }
        logger.warn("load bloom filter from cache failed,use empty filter");
        return filter;
    }

    public boolean isPresent(T id) {
        if (id instanceof String) {
            return bloomFilter.isPresent((String) id);
        } else if (id instanceof Number) {
            return bloomFilter.isPresent(((Number) id).intValue());
        } else if (id instanceof Byte[]) {
            return bloomFilter.isPresent((byte[]) id);
        } else {
            logger.warn("unsupported type:{},id:{}", id.getClass().getName(), id);
            return false;
        }
    }
}
