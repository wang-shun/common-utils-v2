package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.jcache.monitor.Visitor;
import com.youzan.sz.jutil.util.Pair;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
//
//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.monitor.Visitor;
//import com.qq.jutil.util.Pair;

/**
 * 自动刷新的Cache
 *    实现每隔一段时间自动刷新整个缓存数据的机制，主要应用于黑名单判断等；
 *    以黑名单为例，程序不需要对单个缓存数据进行超时维护，只需要每隔一定
 * 的时间重新从数据库读取黑名单，并设置到Cache中即可。
 *    本类使用方法如下：
 *    从AutoRefreshCache派生一个类，重写抽象方法reflush()，该方法返
 * 回一个包含更新缓存数据的ConcurrentHashMap（如通过读取数据库等）
 *    详细例子请参考com.qq.jutil.jcache.test.TestAuto以及配置文件
 * cacheconfig.xml的example_custom_class节
 * 
 * @author meteor
 *
 * @param <K>	key的类型
 * @param <V>	value的类型
 */
@SuppressWarnings("unchecked")
public abstract class AutoRefreshCache<K, V> extends AutoRefreshCacheBase<K, V>
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private static final int MAX_PROCESS_QUEUE_SIZE = 1000;
	
	private static final BlockingQueue<AutoRefreshCache> processQueue = new LinkedBlockingQueue<AutoRefreshCache>();
	
	private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();
	
	private AtomicInteger queryCount = new AtomicInteger(0);
	
	private AtomicInteger hitCount = new AtomicInteger(0);
	
	private static final Thread[] threads = new Thread[1];
	
	private String saveFilePath;												// 持久化保存路径，如果为null，表示不需要持久化
	private boolean autoRecover = false;										// 是否自动从持久化数据中恢复
	private boolean saveToFile;

	public long getReflushTime() {
		return reflushTime;
	}

	private volatile long reflushTime = 0;
	static
	{
		for(int i = 0; i < threads.length; ++i)
		{
			try
			{
				threads[i] = new Thread() 
	            {
					public void run()
	                {
						doReflushProcess();
					}
				};
				threads[i].start();
			}
			catch(Exception e)
			{
				logger.error("AutoRefreshCache init error: ", e);
			}
		}
	}
	
	public AutoRefreshCache()
	{}
	
	public AutoRefreshCache(Properties prop)
	{
		saveFilePath = prop.getProperty("save_file_path");
		if(saveFilePath != null && saveFilePath.length() == 0)
			saveFilePath = null;
		autoRecover = getPropertyBoolean(prop, "auto_recover", false);

		if(autoRecover)
			recover(saveFilePath);
	}
		
	private static boolean getPropertyBoolean(Properties prop, String name, boolean def)
	{
		String s = prop.getProperty(name);
		if("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
			return true;
		if("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s))
			return false;
		return def;
	}
	
	private static void doReflushProcess()
	{
		while(true)
		{
			try
			{
				AutoRefreshCache c = processQueue.take();
				c.doReflush();
			}
			catch (Exception e)
			{
				logger.error("reflush error: ", e);
			}
		}
	}
	
	protected ConcurrentHashMap<K, V> getCacheMap()
	{
		return map;
	}

	protected void doReflush()
	{

		this.queryCount.set(0);
		this.hitCount.set(0);

		long reflushBegin = System.currentTimeMillis();

		ConcurrentHashMap<K, V> m = reflush();

		long reflushEnd = System.currentTimeMillis();
		reflushTime = reflushEnd - reflushBegin;

		if (m != null)
		{
			map = m;
		}
		boolean isSaveToFile = isSaveToFile();
		if (isSaveToFile)
		{
			save(saveFilePath);
		}
		else
		{
			logger.info("SKIP save cache to file:" + saveFilePath);
		}
	}

	/**
	 * 刷新cache，重新生成cache
	 * @return	返回新cache的数据
	 */
	public abstract ConcurrentHashMap<K, V> reflush();
	
	public int cleanUp()
	{
		if(processQueue.size() < MAX_PROCESS_QUEUE_SIZE)
			processQueue.add(this);
		return 0;
	}

	public V get(K key)
	{
		this.queryCount.incrementAndGet();
		V v = map.get(key);
		if(v == null)
			return null;
		this.hitCount.incrementAndGet();
		return v;
	}

	public boolean containsCache(K key)
	{
		return map.containsKey(key);
	}

	public V put(K key, V value)
	{
		return map.put(key, value);
	}

	public void clear()
	{
		//map.clear();
		doReflush();
	}

	public V remove(K key)
	{
		return map.remove(key);
	}

	public int size()
	{
		return map.size();
	}

	public Set<K> keySet()
	{
		return map.keySet();
	}

	public int getQueryCount()
	{
		return this.queryCount.get();
	}

	public int getHitCount()
	{
		return this.hitCount.get();
	}

	public int getMemoryUsage()
	{
		/*
		try
		{
			return MemoryUsage.getDeepMemoryUsage(this);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		return -1;
		*/
		return 0;
	}
	
	public Iterator<Entry<K, V>> iterator()
	{
		return map.entrySet().iterator();
	}
	
	protected boolean recover(String file)
	{
		if(file == null)
			return false;
		FileInputStream in = null;
		ObjectInputStream ois = null;
		try
		{
			File f = new File(file);
			if(!f.exists())
				return false;
            logger.info("start to recover cache from file: " + file);
			long start = System.currentTimeMillis();
			in = new FileInputStream(f);
			ois = new ObjectInputStream(in);
			Pair<K, V> pr = null;
			while(true){
				try{
					pr = (Pair<K, V>) ois.readObject();
				}catch(EOFException e){
					break;
				}
				if(pr == null)
					break;
				put(pr.first, pr.second);
			}
            logger.info("finish recovering cache from file: " + file + ", timeusage="
                            + (System.currentTimeMillis() - start) + "ms");
		}
		catch (Throwable e)
		{
			logger.error("recover file error: " + file, e);
			return false;
		}
		finally
		{
			if(in != null){
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					logger.error("close error: " + file, e);
				}
			}
			if(ois != null){
				try
				{
					ois.close();
				}
				catch (IOException e)
				{
					logger.error("close error: " + file, e);
				}
			}
		}
		return true;
	}
	
	protected boolean save(String file)
	{
		if(file == null)
			return false;
		logger.info("begin save cache to file: " + file);
		FileOutputStream outout = null;
		ObjectOutputStream oos = null;
		try
		{
			outout = new FileOutputStream(file);
			oos = new ObjectOutputStream(outout);
			Set<Entry<K, V>> entrySet = map.entrySet();
			logger.info("cache size: " + entrySet.size() +"\tfile:"+ file);
			Iterator<Entry<K, V>> it = entrySet.iterator();
			while(it.hasNext()){
				Entry<K, V> n = it.next();
				try{
					oos.writeObject(Pair.makePair(n.getKey(), n.getValue()));
				}catch(Exception e){
					logger.error("writeObject to file error: ", e);
				}
			}
			oos.flush();
		}
		catch (Exception e)
		{
			logger.error("save to file error: ", e);
			return false;
		}
		finally
		{
			if(outout != null){
				try
				{
					outout.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
			if(oos != null){
				try
				{
					oos.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
			logger.info("finish save cache to file: " + file);
		}
		
		return true;
	}
	
	public void accept(Visitor v, String name){
		v.visitAutoRefreshCache(this, name);
	}

    public String getReflushingStatus() {
      throw new UnsupportedOperationException("unsupported");
    }

	/**
	 * 判断当前的内存cache数据是否持久化到文件，默认返回的是true
	 * @return
	 */
	protected boolean isSaveToFile()
	{
		return true;
	}
}
