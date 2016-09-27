package com.youzan.sz.jutil.jcache.test;

import com.youzan.sz.jutil.jcache.AutoRefreshCache;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.CacheFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
//
//import com.qq.jutil.jcache.AutoRefreshCache;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.CacheFactory;

public class TestAuto extends AutoRefreshCache<String, String>
{
	private String initKey;
	
	private String initValue;
	
	public TestAuto(Properties prop)
	{
		this.initKey = prop.getProperty("initKey");
		this.initValue = prop.getProperty("initValue");
		this.cleanUp();
	}
	
	public ConcurrentHashMap<String, String> reflush()
	{
		ConcurrentHashMap<String, String> m = new ConcurrentHashMap<String, String>();
		m.put(initKey, initValue);
		return m;
	}

	public static void main(String[] args) throws InterruptedException
	{
		Cache<String, String> c = CacheFactory.getCache("example_custom_class");
		System.out.println(c.get("abc"));
		c.put("ddd", "ccc");
		System.out.println(c.get("ddd"));
		Thread.sleep(5000);
		System.out.println(c.get("ddd"));
		Thread.sleep(5000);
		System.out.println(c.get("ddd"));
		Thread.sleep(5000);
		System.out.println(c.get("ddd"));
		System.out.println("end");
		/*
		try
		{
			Class c = Class.forName("com.qq.jutil.jcache.test.TestAuto");
			Constructor con = c.getConstructor(Properties.class);
			Properties prop = new Properties();
			prop.setProperty("initKey", "abc");
			prop.setProperty("initValue", "defg");
			Object o = con.newInstance(prop);
			System.out.println(o != null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		*/
	}

  @Override
  public String getReflushingStatus() {
    throw new UnsupportedOperationException("not supported");
  }
}
