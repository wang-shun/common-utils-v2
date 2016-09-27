package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Pair;
import com.youzan.sz.jutil.util.Tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.CommonCache;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Pair;
//import com.qq.jutil.util.Tools;

/**
 * 能定时将cache保存到磁盘并可以在重启后自动从磁盘还原的cache,此cache继承于CommonCache.<br/>
 * 注意:cache中保存的类必须实现Serializable接口<br/>
 * PersistentCache新增的配置项：<br>
 * save_interval:定时保存到磁盘的时间间隔,单位(秒)<br>
 * cache_file_path:cache保存时的文件名<br>
 * auto_recover:是否在重启后自动从磁盘还原cache<br>
 * @author stonexie
 * @param <K>
 * @param <V>
 */
@Deprecated
@SuppressWarnings("serial")
public class PersistentCache<K, V> extends CommonCache<K, V> implements Serializable
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private long saveInterval;
	private String cacheFilePath;
	
	public PersistentCache(Properties pro)
	{	
		String cleanupStrategyStr = pro.getProperty("cleanup_strategy");
		long timeoutMS = StringUtil.convertLong(pro.getProperty("timeout"),-1) * 1000;
		int maxSize = StringUtil.convertInt(pro.getProperty("max_size"),-1);
		double cleanupRate = StringUtil.convertInt(pro.getProperty("cleanup_rate"),-1) * 0.01;
		int maxMemory = -1;
		int maxCacheObjectSize = -1;
		this.saveInterval = StringUtil.convertLong(pro.getProperty("save_interval"),-1) * 1000;
		this.cacheFilePath = Tools.getFilePath( pro.getProperty("cache_file_path") );
		boolean autoRecover = "true".equals(pro.getProperty("auto_recover"));
		int cleanupStrategy = CLEAN_BY_CREATE_TIME;
		if(cleanupStrategyStr == null)
		{
			throw new IllegalArgumentException("Wrong cleanup_strategy");
		}
		if("create_time".equals(cleanupStrategyStr))
		{
			cleanupStrategy = CLEAN_BY_CREATE_TIME;
		}
		else if("last_access_time".equals(cleanupStrategyStr))
		{
			cleanupStrategy = CLEAN_BY_LASTACCESS_TIME;
		}
		else
		{
			throw new IllegalArgumentException("Wrong cleanup_strategy");
		}
		if(timeoutMS <= 0)
		{
			throw new IllegalArgumentException("Wrong timeoutMS");
		}
		if(maxSize <= 0)
		{
			throw new IllegalArgumentException("Wrong max_size");
		}
		if(cleanupRate <= 0)
		{
			throw new IllegalArgumentException("Wrong cleanup_rate");
		}
		if(saveInterval <= 0)
		{
			throw new IllegalArgumentException("Wrong save_interval");
		}
		if( cacheFilePath.trim().length() == 0 )
		{
			throw new IllegalArgumentException("Wrong cache_file_path");
		}
	
		super.init(cleanupStrategy, timeoutMS, maxSize, cleanupRate, maxMemory,maxCacheObjectSize);	
		if(autoRecover)
		{
			recover();
		}
		
		
		Thread saveThread = new Thread()
		{
			public void run()
			{
				while(true)
				{
					try
					{
						Thread.sleep(saveInterval);
						save();
					}
					catch (Exception e)
					{
						logger.error("", e);
					}
				}
			}
		};	
		
		saveThread.start();
	}
	
	/**
	 * 保存cache到磁盘
	 */
	public void save()
	{
		//System.out.println("[PersistenCache]save:"+ this.cacheFilePath);
		File f = new File(this.cacheFilePath);
		FileOutputStream outout = null;
		ObjectOutputStream oos = null;
		try
		{
			outout = new FileOutputStream(f);
			oos = new ObjectOutputStream(outout);
			Iterator<Map.Entry<K, V>> it = this.iterator();
			List<Pair<K, V>> list = new ArrayList<Pair<K, V>>(size());
			while(it.hasNext())
			{
				Map.Entry<K, V> en = it.next();
				list.add(new Pair<K, V>(en.getKey(),en.getValue()));
			}
			oos.writeObject(list);
			oos.flush();
		}
		catch (Exception e)
		{
			logger.error("", e);
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
		}
	}
	
	/**
	 * 从磁盘还原cache
	 */
	@SuppressWarnings("unchecked")
	private void recover()
	{
		System.out.println("[PersistenCache]recover:"+ this.cacheFilePath);
		File f = new File(this.cacheFilePath);
		if(!f.exists())
		{
			return;
		}
		FileInputStream in = null;
		ObjectInputStream ois = null;
		try
		{		
			in = new FileInputStream(f);
			ois = new ObjectInputStream(in);
/*			PersistentCache<K, V> cache = (PersistentCache<K, V>)ois.readObject();
			if(cache == null)
			{
				System.out.println("[PersistenCache]recover,cache is null");
				return;
			}
			else
			{
				System.out.println("[PersistenCache]recover,cache size:"+ cache.size());
			}*/
			//Iterator<Map.Entry<K, V>> it = cache.iterator();
			List<Pair<K, V>> list = (List<Pair<K, V>>)ois.readObject();
			for(Pair<K, V> en : list)
			{
				put(en.first, en.second);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
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
					logger.error("", e);
				}
			}
			if(ois != null){
				try
				{
					ois.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
		}
		System.out.println("[PersistenCache]after recover,size:"+ this.size());
	}	
}
