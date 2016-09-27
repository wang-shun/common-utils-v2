package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.thread.FixThreadExecutor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.thread.FixThreadExecutor;

@Deprecated
public abstract class AutoSaveCache<K, V> extends CommonCache<K, V>
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private FixThreadExecutor tp;
	
	public AutoSaveCache(int cleanupStrategy, long timeoutMS, int maxSize,
			double cleanupRate, int maxMemory, int maxCacheObjectSize, int threadCount)
	{
		init(cleanupStrategy, timeoutMS, maxSize, cleanupRate, maxMemory, maxCacheObjectSize, threadCount);
	}

	private void init(int cleanupStrategy, long timeoutMS, int maxSize, double cleanupRate, int maxMemory, int maxCacheObjectSize, int threadCount)
	{
		init(cleanupStrategy, timeoutMS, maxSize, cleanupRate, maxMemory, maxCacheObjectSize);
		tp = new FixThreadExecutor(threadCount, 1000000);
		//setCleanUpLoopCount(1);
	}
	
	public AutoSaveCache(Properties prop)
	{
		String cleanup_strategy = prop.getProperty("cleanup_strategy");
		long timeout = StringUtil.convertLong(prop.getProperty("timeout"), 60 * 60 * 1000);
		timeout *= 1000; // 转换成毫秒
		int maxSize = StringUtil.convertInt(prop.getProperty("max_size"), 10000);
		int maxMemory = StringUtil.convertInt(prop.getProperty("max_memory"), -1);
		maxMemory *= 1024 * 1024;

		int cleanup_rate = StringUtil.convertInt(prop.getProperty("cleanup_rate"), 30);
		double cleanupRate = ((double) cleanup_rate) / 100;

		int cleanupStrategy = CommonCache.CLEAN_BY_LASTACCESS_TIME;
		if ("create_time".equalsIgnoreCase(cleanup_strategy))
		{
			cleanupStrategy = CommonCache.CLEAN_BY_CREATE_TIME;
		}

		int maxCacheObjectSize = StringUtil.convertInt(prop.getProperty("max_cache_object_size"), -1);

		int threadCount = StringUtil.convertInt(prop.getProperty("thread_count"), 1);
		init(cleanupStrategy, timeout, maxSize, cleanupRate, maxMemory, maxCacheObjectSize, threadCount);
	}
	
	public int cleanUp()
	{
		long maxLifeTime = this.timeout;
		int removeCount = 0;
		try
		{
			int remainSize = maxSize;
			if (map.size() > maxSize)
				remainSize = (int) ((1 - cleanupRate) * maxSize); //保留的map的个�?
			Set<K> inProcess = new HashSet<K>();

			for (int i = 0; i < cleanUpLoopCount; i++) //�?多尝�?10�?,使得将缓存数目减至合适的
			{
				long now = System.currentTimeMillis();
				Set<Entry<K, CacheValue<V>>> entrySet = map.entrySet();
				Iterator<Entry<K, CacheValue<V>>> it = entrySet.iterator();

				while (it.hasNext())
				{
					final Entry<K, CacheValue<V>> entry = it.next();
					final K key = entry.getKey();
					final CacheValue<V> v = entry.getValue();
					if (now - v.time > maxLifeTime) //如果生命期超过了限制时间
					{
						try{
							if(inProcess.add(key)){
								tp.execute(new Runnable(){
									public void run()
									{
										saveAndRemove(key, v.value);
									}
								});
							}
						}catch(Exception e){
							logger.error("", e);
						}
						if(i > 0 && map.size() - inProcess.size() <= remainSize)	// 第二遍时只清到remainSize
						{
							break;
						}
					}
				}

				if (map.size() - inProcess.size() <= remainSize
						&& currentMemoryUsage.get() < maxMemory) //如果map的size已经在保留�?�以�?,直接中断循环
				{
					break;
				}

				maxLifeTime = maxLifeTime / 2; //超时时间减为原先的一�?.
			}
		}
        catch(Exception e)
        {
        	logger.error("cleanUp error: ", e);
        }
        return removeCount;
	}
	
	protected abstract void saveAndRemove(K key, V value);
	
	public void clear()
	{
		try
		{
			Iterator<Entry<K, V>> it = iterator();
			while (it.hasNext())
			{

				final Entry<K, V> entry = it.next();
				final V v = entry.getValue();
				try
				{
					tp.execute(new Runnable()
					{
						public void run()
						{
							saveAndRemove(entry.getKey(), v);
						}
					});
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("clear error: ", e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub

	}

}
