package com.youzan.sz.jutil.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//import com.qq.jutil.util.Pair;

@SuppressWarnings("serial")
public class MappedQueue<K,V> implements Serializable
{
	public static final int STAUTS_NULL = -2;
	public static final int STAUTS_FULL = -1;	
	public static final int STAUTS_INQUEUE = 0;  
	public static final int STAUTS_OK = 1; 
	
	private LinkedList<Pair<K,V>> queue;
	private HashMap<K,V> map;
	private final int capacity;
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final Lock r = rwLock.readLock();
	private final Lock w = rwLock.writeLock();	
	
	public MappedQueue(int capacity)
	{
		this.queue = new LinkedList<Pair<K,V>>();
		this.map = new HashMap<K,V>();
		this.capacity = capacity;
	}
	
	
	/**
	 * 如果可能，在队列尾部插入指定的元素，如果队列已满则立即返回。
	 * @param key
	 * @param value
	 * @return int
	 */
	public int offer(Pair<K,V> value)
	{
		K key = value.first;
		w.lock();
		try
		{
			if(value.second == null)
				return STAUTS_NULL;
			
			if(this.queue.size() >= this.capacity)
				return STAUTS_FULL;
			
			if(this.map.containsKey(key))
				return STAUTS_INQUEUE;		
			
			this.queue.offer(value);
			this.map.put(key, value.second);
			
			return STAUTS_OK;
		}
		finally
		{
			w.unlock();
		}		
	}
	
	/**
	 * 检索，但是不移除此队列的头，如果此队列为空，则返回 null。
	 * @return Pair<K,V>
	 */
	public Pair<K,V> peek()
	{	
		r.lock();
		try
		{		
			return this.queue.peek();	
		}
		finally
		{
			r.unlock();
		}				
	}	
	
	/**
	 * 检索并移除此队列的头，如果此队列为空，则返回 null。
	 * @return Pair<K,V>
	 */
	public Pair<K,V> poll()
	{
		w.lock();
		try
		{		
			Pair<K,V> data = this.queue.poll();
			if(data != null)
			{
				this.map.remove(data.first);
			}
			return data;
		}
		finally
		{
			w.unlock();
		}		
	}
	
	/**
	 * 返回理想情况下（没有内存和资源约束）此队列可接受的元素数量。
	 * @return int
	 */
	public int remainingCapacity()
	{		
		r.lock();
		try
		{			
			return this.capacity - this.queue.size();		
		}
		finally
		{
			r.unlock();
		}			
	}	
	
	/**
	 * 返回队列中的元素个数。
	 * @return int
	 */
	public int size()
	{	
		r.lock();
		try
		{		
			return this.queue.size();		
		}
		finally
		{
			r.unlock();
		}		
	}	
	
	/**
	 * 队列是否空
	 * @return boolean
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	/**
	 * 队列中是否已存在此元素
	 * @param key
	 * @return boolean
	 */
	public boolean isInqueue(K key)
	{
		r.lock();
		try
		{
			return this.map.containsKey(key);
		}
		finally
		{
			r.unlock();
		}
	}
	
	/**
	 * 查找元素但不移除,如果元素不存在则返回null
	 * @param key
	 * @return V
	 */
	public V find(K key)
	{
		r.lock();
		try
		{
			return this.map.get(key);
		}
		finally
		{
			r.unlock();
		}		
	}
	
	/**
	 * 移除队我中关键字为key的元素,并返回此元素,不存在则返回null
	 * @param key
	 * @return V
	 */
	public V remove(K key)
	{
		V data = null;
		w.lock();
		try
		{
			data = this.map.get(key);
			if(data != null)
			{
				this.map.remove(key);
				Iterator<Pair<K,V>> it = this.queue.iterator();
				while(it.hasNext())
				{
					Pair<K,V> p = it.next();
					if(p.first.equals(key))
					{
						it.remove();
						break;
					}
				}
			}
		}
		finally
		{
			w.unlock();
		}
		return data;
	}
	
	/**
	 * 从队列彻底移除所有元素。
	 */
	public void clear()
	{
		w.lock();
		try
		{
			this.queue.clear();
			this.map.clear();
		}
		finally
		{
			w.unlock();
		}		
	}
	
	public Iterator<V> iterator()
	{
		return map.values().iterator();
	}
}
