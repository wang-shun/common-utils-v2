package com.youzan.sz.common.util.bloom;

import static com.youzan.sz.common.util.TimeCostWrapper.doTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.youzan.sz.common.util.DateUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.util.CollectionUtils;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.jutil.bloomfilter.BloomFilter;

/**
 *
 * Created by zhanguo on 2016/9/23.
 */
public abstract class BaseBloomProducerHelper<T> extends BaseBloomHelper<T> {

    /**
     * 检查redis是否过期->没过期则从数据库拉取
     *
     * */
    @Override
    public Boolean load() {
        //这里只标示上次加载时间,数据考虑到网络流量暂存本地
        logger.info("load bloom filter with config:{}", JsonUtils.toJson(getConfig()));
        logger.info("load bloom filter as producer ");
        final String cacheKey = getConfig().getRefreshMutex();
        final String loadDBTimeKey = getConfig().getLoadDBTimeKey();
        final long now = System.currentTimeMillis();
        //避免执行时时间过长,持有锁down掉,所以设置一个过期时间
        final Boolean setnx = jedisTemplate.setnxex(cacheKey, String.valueOf(now), TimeUnit.MINUTES.toSeconds(30));
        try {
            if (setnx) {//只有一台能拿取到锁,并重新加载
                final String loadTime = jedisTemplate.get(loadDBTimeKey);
                long lastedLoadDBTime = 0;
                if (StringUtil.isNotEmpty(loadTime)) {
                    lastedLoadDBTime = Long.valueOf(loadTime);
                }
                logger.info("current machine get mutex   to load bloomFilter");

                boolean needReload = false;
                if(DateUtils.getTodayStr().equals("161103") || DateUtils.getTodayStr().equals("161104")
                        || DateUtils.getTodayStr().equals("161105")){
                    logger.info("reload admin from db ,for {} " +  DateUtils.getTodayStr());
                    final String cursorCacheKey = getConfig().getCacheCursor();
                    jedisTemplate.del(cursorCacheKey);
                    needReload = true;
                }

                if ((now - lastedLoadDBTime > TimeUnit.DAYS.toMillis(1)) || needReload) {//1天load一次
                    logger.info("it's time   to load bloomFilter from db");
                    this.bloomFilter = refreshFilter();
                    //设置新的load时间
                    jedisTemplate.set(loadDBTimeKey, String.valueOf(now));
                    return true;
                }
            }
            logger.info("current machine not  get mutex or it's not the time ,so to load bloomFilter from cache");
            this.bloomFilter = loadFromCache();
            return true;
        } finally {
            if (setnx)//如果有拉取到锁,需要删除掉锁
                jedisTemplate.del(cacheKey);
        }
    }

    private BloomFilter refreshFilter() {
        final String bloomCacheKey = getConfig().getCacheKey();
        final String cursorCacheKey = getConfig().getCacheCursor();
        final String bloomCache = doTask(() -> jedisTemplate.get(bloomCacheKey), TimeUnit.MINUTES.toSeconds(1));
        Long startCursor;
        BloomFilter filter = BloomFilter.getFilter(getConfig().getNumOfElements(), getConfig().getProbability());
        if (StringUtil.isEmpty(bloomCache)) {//没有命中,需要重新加载
            startCursor = 0L;
        } else {
            startCursor = jedisTemplate.getAsLong(cursorCacheKey);
            if (startCursor == null || startCursor < 0) {
                startCursor = 0L;
            } else {//由于bloom只是用bit数据,所以可以直接添加bytes
                filter.add(bloomCache.getBytes());
            }
        }
        //load new Data from DB
        List<T> ids;
        while (CollectionUtils.isNotEmpty(ids = loadFromDB(startCursor))) {
            for (T id : ids) {
                if (id instanceof String) {
                    filter.add((String) id);
                } else if (id instanceof Number) {
                    filter.add(((Number) id).intValue());
                } else if (id instanceof Byte[]) {
                    filter.add((byte[]) id);
                } else {
                    logger.warn("unsupported type:{},id:{}", id.getClass().getName(), id);
                }
            }
            if (logger.isInfoEnabled())
                logger.info("add adminId:{} to filter,current startCursor:{}", JsonUtils.toJson(ids), startCursor);
            startCursor += getConfig().getNumOfPerLoad();
        }
        //save cache to redis
        final String base64ByteStr = Base64.encodeBase64String(filter.getBitBuffer().toArray());
        //        jedisTemplate.set("", )
        doTask(() -> jedisTemplate.setex(bloomCacheKey, base64ByteStr, (int) TimeUnit.DAYS.toSeconds(10)), 60);
        logger.info("save filter:{} to cache ,hash count:{},bytesLen:{}", getConfig().getBloomTopic(),
            filter.getHashCount(), base64ByteStr.length());
        //save cursor
        logger.info("load start cursor:{} success", startCursor);
        startCursor = Math.max(0, startCursor - getConfig().getNumOfPerLoad());//避免最后一次没有load满,所以这里退回一个
        logger.info("save start cursor:{}", startCursor);
        jedisTemplate.set(cursorCacheKey, startCursor.toString());
        return filter;
    }

    protected abstract List<T> loadFromDB(Long start);

    public static void main(String[] args) {
        int start = 1_600_000;
        int num = 1_000_000;
        //        int num = 1_000;
        final long startDate = System.currentTimeMillis();
        final BloomFilter bloomFilter = BloomFilter.getFilter(num, 0.01);
        for (int i = 0; i < num; i++) {
            if (i % 1000 == 0) {
                System.out.println("add to " + i);
            }
            bloomFilter.add(start + i);
        }
        final byte[] bytes = bloomFilter.getBitBuffer().toBytes();
        System.out.println("count:" + num + ",len:" + bytes.length);
        System.out.println("cost:" + (System.currentTimeMillis() - startDate) / 1000 + "s");
        final String filePath = "/Users/vincentbu/IdeaProjects/portal/portal-deploy/src/main/resources/admin_id.dat";
        try {
            final File file = new File(filePath);
            FileUtils.writeByteArrayToFile(file, bytes, false);
            System.out.println("file len:" + file.length());
            System.out.println("file path:" + file.getAbsolutePath());
            System.out.println("file path:" + file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
