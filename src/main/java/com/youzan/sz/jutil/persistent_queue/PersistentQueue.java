package com.youzan.sz.jutil.persistent_queue;

import com.youzan.sz.jutil.util.Pair;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//import com.qq.jutil.persistent_queue.PersistentQueueImpl;
//import com.qq.jutil.util.Pair;

public final class PersistentQueue
{
	public static int DEFALT_MAX_SIZE = 10000;

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock r = rwLock.readLock();

	private final Lock w = rwLock.writeLock();

	private PersistentQueueImpl wb;

	public PersistentQueue(String file, int initBlockCount, int blockSize, int maxBlockCount, int maxQueueSize) throws IOException
	{
		w.lock();
		try
		{
			wb = new PersistentQueueImpl(file, initBlockCount, blockSize, maxBlockCount, maxQueueSize);
		}
		finally
		{
			w.unlock();
		}
	}
	public PersistentQueue(String file, int maxSize) throws IOException
	{
		w.lock();
		try
		{
			wb = new PersistentQueueImpl(file, maxSize);
		}
		finally
		{
			w.unlock();
		}
	}

	public PersistentQueue(String file) throws IOException
	{
		this(file, DEFALT_MAX_SIZE);
	}

	public void append(String key, byte[] appendData)
	{
		w.lock();
		try
		{
			wb.append(key, appendData);
		}
		finally
		{
			w.unlock();
		}
	}

	public int hashCode()
	{
		return wb.hashCode();
	}

	public boolean equals(Object arg0)
	{
		return wb.equals(arg0);
	}

	public String toString()
	{
		return wb.toString();
	}

	public byte[] get(String key)
	{
		r.lock();
		try
		{
			return wb.get(key);
		}
		finally
		{
			r.unlock();
		}
	}

	public boolean contain(String key)
	{
		r.lock();
		try
		{
			return wb.contain(key);
		}
		finally
		{
			r.unlock();
		}
	}

	public Pair<String, byte[]> pop()
	{
		w.lock();
		try
		{
			return wb.pop();
		}
		finally
		{
			w.unlock();
		}
	}

	public void put(String key, byte[] value)
	{
		w.lock();
		try
		{
			wb.put(key, value);
		}
		finally
		{
			w.unlock();
		}
	}

	public void remove(String key)
	{
		w.lock();
		try
		{
			wb.remove(key);
		}
		finally
		{
			w.unlock();
		}
	}

	public int size()
	{
		r.lock();
		try
		{
			return wb.size();
		}
		finally
		{
			r.unlock();
		}
	}

	public boolean isEmpty()
	{
		r.lock();
		try
		{
			return wb.isEmpty();
		}
		finally
		{
			r.unlock();
		}
	}

	public Pair<String, byte[]>[] toArray()
	{
		r.lock();
		try
		{
			return wb.toArray();
		}
		finally
		{
			r.unlock();
		}
	}
	
	public static void main(String[] argv)
	{
		
	}
}
