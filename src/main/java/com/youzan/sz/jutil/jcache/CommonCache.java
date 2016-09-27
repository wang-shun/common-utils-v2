package com.youzan.sz.jutil.jcache;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.youzan.sz.jutil.j4log.Logger;
//import com.qq.jutil.j4log.Logger;

@SuppressWarnings("serial")
public class CommonCache<K, V> implements Cache<K, V>, Serializable
{
	public static final int CLEAN_BY_CREATE_TIME = 0;
	public static final int CLEAN_BY_LASTACCESS_TIME = 1;
	
	private static final Logger logger = Logger.getLogger("jutil");
	
	private int cleanupStrategy;			// 淘汰策略，CLEAN_BY_CREATE_TIME/CLEAN_BY_LASTACCESS_TIME
	
	protected long timeout;					// 超时时间
	protected int maxSize;					// 元素个数最大值
	protected int maxMemory;				// 最大占用内存（已去掉该功能）
	protected double cleanupRate;			// 清除率，当cache达到最大元素个数时，清除的百分比
	protected int maxCacheObjectSize;		// 单个元素最大占用内存大小（已去掉该功能）
	protected int cleanUpLoopCount = 10;	// 清除轮数
	
	private AtomicInteger queryCount = new AtomicInteger();	// 查询次数
	private AtomicInteger hitCount = new AtomicInteger();	// 命中次数
	
	protected AtomicInteger currentMemoryUsage = new AtomicInteger();	// 当前内存使用量
	
	protected Map<K, CacheValue<V> > map = new ConcurrentHashMap<K, CacheValue<V> >();	// cache数据

	protected CommonCache()
	{
	}
	
	public CommonCache(int cleanupStrategy, long timeoutMS, int maxSize,
			double cleanupRate, int maxMemory, int maxCacheObjectSize)
	{
		init(cleanupStrategy, timeoutMS, maxSize, cleanupRate, maxMemory, maxCacheObjectSize);
	}
	
	protected void setCleanUpLoopCount(int count)
	{
		cleanUpLoopCount = count;
	}
	
	protected int getCleanUpLoopCount()
	{
		return cleanUpLoopCount;
	}

	protected void init(int cleanupStrategy, long timeoutMS, int maxSize, double cleanupRate, int maxMemory, int maxCacheObjectSize)
	{
		this.cleanupStrategy = cleanupStrategy;
		this.timeout = timeoutMS;
		if(this.timeout <= 0)
			this.timeout = Long.MAX_VALUE;
		this.maxSize = maxSize;
		if(this.maxSize <= 0)
			this.maxSize = Integer.MAX_VALUE;
		this.cleanupRate = cleanupRate;
		//this.maxMemory = maxMemory;
		//if(this.maxMemory <= 0)
			//this.maxMemory = Integer.MAX_VALUE;
		this.maxMemory = Integer.MAX_VALUE;
		//this.maxCacheObjectSize = maxCacheObjectSize;
		//if(this.maxCacheObjectSize <= 0)
		//	this.maxCacheObjectSize = Integer.MAX_VALUE;
		this.maxCacheObjectSize = Integer.MAX_VALUE;
	}
	
	public V get(K key)
	{
		queryCount.incrementAndGet();
		CacheValue<V> v = map.get(key);
		if(v == null)
		{
			return null;
		}
		hitCount.incrementAndGet();
		if(this.cleanupStrategy == CLEAN_BY_LASTACCESS_TIME)
		{
			v.time = System.currentTimeMillis();
		}
		return v.value;
	}

	public V put(K key, V value)
	{
		CacheValue<V> v = new CacheValue<V>();
		v.time = System.currentTimeMillis();
		v.value = value;
		v.memoryUsage = getMemoryUsage(value);
		if(v.memoryUsage > maxCacheObjectSize)
			return null;
		CacheValue<V> v2 = map.put(key, v);
		currentMemoryUsage.addAndGet(v.memoryUsage);
		if(v2 == null)
			return null;
		currentMemoryUsage.addAndGet(-v2.memoryUsage);
		return v2.value;
	}

	private final int getMemoryUsage(Object o)
	{
		/*
		if(maxCacheObjectSize == Integer.MAX_VALUE
				&& maxMemory == Integer.MAX_VALUE)
			return 0;
		try
		{
			return MemoryUsage.getDeepMemoryUsage(o);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		*/
		return 0;
	}

	public void clear()
	{
		map.clear();
		currentMemoryUsage.set(0);
	}

	public V remove(K key)
	{
		CacheValue<V> v = map.remove(key);
		if(v == null)
			return null;
		currentMemoryUsage.addAndGet(-v.memoryUsage);
		return v.value;
	}
	
	public int size()
	{
		return map.size();
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

			for (int i = 0; i < cleanUpLoopCount; i++) //�?多尝�?10�?,使得将缓存数目减至合适的
			{
				long now = System.currentTimeMillis();
				Set<Entry<K, CacheValue<V>>> entrySet = map.entrySet();
				Iterator<Entry<K, CacheValue<V>>> it = entrySet.iterator();

				while (it.hasNext())
				{
					Entry<K, CacheValue<V>> entry = it.next();
					CacheValue<V> v = entry.getValue();
					if (now - v.time > maxLifeTime) //如果生命期超过了限制时间
					{
						if(needToRemove(entry.getKey(), v)){
							it.remove(); //移走该对�??
							currentMemoryUsage.addAndGet(-v.memoryUsage);
							removeCount++;
						}
						if(i > 0 && map.size() <= remainSize)	// 第二遍时只清到remainSize
						{
							break;
						}
					}
				}

				if (map.size() <= remainSize
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
	
	protected boolean needToRemove(K key, CacheValue<V> value)
	{
		return true;
	}

	public int getQueryCount()
	{
		return queryCount.get();
	}

	public int getHitCount()
	{
		return hitCount.get();
	}

	public Set<K> keySet()
	{
		return map.keySet();
	}
	
	public int getMemoryUsage()
	{
		if(maxCacheObjectSize == Integer.MAX_VALUE
				&& maxMemory == Integer.MAX_VALUE)
			return -1;
		return currentMemoryUsage.get();
	}

	public boolean containsCache(K key)
	{
		//return map.containsKey(key);
		return get(key) != null;
	}

	public Iterator<Entry<K, V>> iterator()
	{
		return new CacheIterator<K, V>(map.entrySet().iterator());
	}
}
