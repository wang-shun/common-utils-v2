package com.youzan.sz.jutil.jcache;

import java.util.Iterator;
import java.util.Map;

final class CacheIterator<K, V> implements Iterator<Map.Entry<K, V>>
{
	private Iterator<Map.Entry<K,CacheValue<V>>> it;
	
	private static class Entry<Key, Value> implements Map.Entry<Key, Value>
	{
		private Key k;
		private Value v;
		
		public Entry(Key k, Value v)
		{
			this.k = k;
			this.v = v;
		}

		public Key getKey()
		{
			return k;
		}

		public Value getValue()
		{
			return v;
		}

		public Value setValue(Value v)
		{
			Value o = this.v;
			this.v = v;
			return o;
		}
	}
	
	public CacheIterator(Iterator<Map.Entry<K,CacheValue<V>>> it)
	{
		this.it = it;
	}

	public boolean hasNext()
	{
		return it.hasNext();
	}

	public Entry<K, V> next()
	{
		Map.Entry<K, CacheValue<V>> e = it.next();
		return new Entry<K, V>(e.getKey(), e.getValue().value);
	}

	public void remove()
	{
		it.remove();
	}
	
	public String toString()
	{
		return it.toString();
	}
}
