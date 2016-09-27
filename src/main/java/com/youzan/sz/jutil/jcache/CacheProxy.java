package com.youzan.sz.jutil.jcache;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

final class CacheProxy<K, V> implements Cache<K, V>
{
	Cache<K, V> c = null;

	public int cleanUp()
	{
		return c == null ? 0 : c.cleanUp();
	}

	public void clear()
	{
		if(c != null)
			c.clear();
	}

	public boolean containsCache(K key)
	{
		return c == null ? false : c.containsCache(key);
	}

	public V get(K key)
	{
		return c == null ? null : c.get(key);
	}

	public int getHitCount()
	{
		return c == null ? 0 : c.getHitCount();
	}

	public int getMemoryUsage()
	{
		return c == null ? 0 : c.getMemoryUsage();
	}

	public int getQueryCount()
	{
		return c == null ? 0 : c.getQueryCount();
	}

	public Iterator<Entry<K, V>> iterator()
	{
		return c == null ? null : c.iterator();
	}

	public Set<K> keySet()
	{
		return c == null ? null : c.keySet();
	}

	public V put(K key, V value)
	{
		return c == null ? null : c.put(key, value);
	}

	public V remove(K key)
	{
		return c == null ? null : c.remove(key);
	}

	public int size()
	{
		return c == null ? 0 : c.size();
	}
}
