package com.youzan.sz.jutil.ha;

import com.youzan.sz.jutil.j4log.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.Logger;

/**
 * 流量管制器.
 * 用于接口过载保护.
 * @author stonexie
 *
 */
public class TrafficRestrict
{
	private static Logger                             LOGGER     = Logger.getLogger("traffice_log");
	private static ConcurrentHashMap<String, Setting> counterMap = new ConcurrentHashMap<String, Setting>();
	static
	{
		//定期清0计数器线程
		Thread t = new Thread()
		{
			public void run()
			{
				int count = 0;
				while(true)
				{
					try
					{
						Thread.sleep(1000);
						for(String key : counterMap.keySet())
						{
							Setting set = counterMap.get(key);
							if(count % set.interval == 0)
							{
								LOGGER.info(key +"\t"+ set.count.get());
								set.lastCount = set.count.get();
								set.count.set(0);
							}
						}
						count++;
					} 
					catch (Throwable e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}
	
	/**
	 * 检查此接口是否能够服务,如果超过限制值则返回false
	 * @param name 接口名
	 * @return boolean 是否应该服务
	 */
	public static boolean tryServe(String name)
	{
		Setting set = counterMap.get(name);
		if(set != null)
		{
			if(set.count.get() < set.limit)
			{
				set.count.addAndGet(1);
				return true;
			}
			else
			{
				set.count.addAndGet(1);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 注册一个流量控制器
	 * @param name 名字
	 * @param interval 单位时间
	 * @param limit 单位时间内的流量限制
	 */
	public static void regiest(String name, int interval, int limit)
	{
		counterMap.put(name, new Setting(interval, limit));
	}
	
	/**
	 * 取消一个流量控制器
	 * @param name
	 * @param interval
	 * @param limit void
	 */
	public static void unregiest(String name, int interval, int limit)
	{
		counterMap.remove(name);
	}
	
	static class Setting
	{
		int interval;
		int limit;
		AtomicInteger count;
		int lastCount;
		public Setting(int interval, int limit)
		{
			this.interval = interval;
			this.limit = limit;
			this.count = new AtomicInteger(0);
			this.lastCount = 0;
		}
	}
	
	public static String getStat()
	{
		String str = "service\tnowRequest\tlastRequest\n";
		for(String key : counterMap.keySet())
		{
			Setting set = counterMap.get(key);
			str += key +"\t"+ set.count.get() +"\t"+ set.lastCount +"\n";
		}
		return str;
	}
	
	public static void main(String[] args)
	{
		TrafficRestrict.regiest("test", 10, 5);
		for (int i = 0; i < 1000; i++)
		{
			System.out.println(TrafficRestrict.tryServe("test"));
		}
		System.out.println(TrafficRestrict.getStat());
	}
}
