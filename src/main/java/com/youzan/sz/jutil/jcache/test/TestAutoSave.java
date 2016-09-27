package com.youzan.sz.jutil.jcache.test;

import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.CacheFactory;
import com.youzan.sz.jutil.jcache.adv.AdvAutoSaveCache;

import java.util.Properties;

//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.CacheFactory;
//import com.qq.jutil.jcache.adv.AdvAutoSaveCache;

@SuppressWarnings("serial")
public class TestAutoSave<K, V> extends AdvAutoSaveCache<K, V>
{
	public TestAutoSave(Properties prop)
	{
		super(prop);
	}

	protected void saveAndRemove(K key, V value)
	{
		System.out.println("Key: " + key + "\tValue" + value);
		remove(key);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Cache<String, String> c = CacheFactory.getCache("example_autosave_cache");
		for(int i = 0; i < 100; ++i){
			c.put("abc", "efg");
			c.put("ab34c", "efgvg");
			c.put("ab5c", "ebgfg");
		}
	}
}
