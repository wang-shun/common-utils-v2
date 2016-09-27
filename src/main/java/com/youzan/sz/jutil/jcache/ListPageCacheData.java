package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.data.DataPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//import com.qq.jutil.data.DataPage;

/**
 * 列表页缓存
 * 不推荐使用，该类的设计不合理，请考虑使用ListCacheData代替
 * @author meteor 用于缓存列表页数据，只缓存前面的n条数据
 * @param <E>
 */
@SuppressWarnings("serial")
@Deprecated
public class ListPageCacheData<E> implements Serializable
{
	private ListPageCacheDataImpl<E> ls;

	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private final Lock r = rwLock.readLock();

	private final Lock w = rwLock.writeLock();
/*
	public ListPageCacheData(int maxSize, int totalRecordCount)
	{
		w.lock();
		try
		{
			ls = new ListPageCacheDataImpl<E>(maxSize, totalRecordCount);
		}
		finally
		{
			w.unlock();
		}
	}
*/
	public ListPageCacheData(List<E> data, int totalRecordCount, boolean autoChangeTotalRecordCount)
	{
		w.lock();
		try
		{
			ls = new ListPageCacheDataImpl<E>(data, totalRecordCount, autoChangeTotalRecordCount);
		}
		finally
		{
			w.unlock();
		}
	}

	public ListPageCacheData(List<E> data, int maxSize, int totalRecordCount, boolean autoChangeTotalRecordCount)
	{
		w.lock();
		try
		{
			ls = new ListPageCacheDataImpl<E>(data, maxSize, totalRecordCount, autoChangeTotalRecordCount);
		}
		finally
		{
			w.unlock();
		}
	}
	
	public ListPageCacheData(List<E> data, int totalRecordCount)
	{
		this(data, totalRecordCount, true);
	}

	public ListPageCacheData(List<E> data, int maxSize, int totalRecordCount)
	{
		this(data, maxSize, totalRecordCount, true);
	}

	public E add(E e)
	{
		w.lock();
		try
		{
			return ls.add(e);
		}
		finally
		{
			w.unlock();
		}
	}

	public E add(int n, E e)
	{
		w.lock();
		try
		{
			return ls.add(n, e);
		}
		finally
		{
			w.unlock();
		}
	}

	public void clear()
	{
		w.lock();
		try
		{
			ls.clear();
		}
		finally
		{
			w.unlock();
		}
	}

	public void moveTo(E src, int destPos)
	{
		w.lock();
		try
		{
			ls.moveTo(src, destPos);
		}
		finally
		{
			w.unlock();
		}
	}

	public void moveTo(int srcPos, int destPos)
	{
		w.lock();
		try
		{
			ls.moveTo(srcPos, destPos);
		}
		finally
		{
			w.unlock();
		}
	}

	public E moveToTop(E e)
	{
		w.lock();
		try
		{
			return ls.moveToTop(e);
		}
		finally
		{
			w.unlock();
		}
	}

	public void moveToTop(int n)
	{
		w.lock();
		try
		{
			ls.moveToTop(n);
		}
		finally
		{
			w.unlock();
		}
	}

	public E remove(E e)
	{
		w.lock();
		try
		{
			return ls.remove(e);
		}
		finally
		{
			w.unlock();
		}
	}

	public E remove(int n)
	{
		w.lock();
		try
		{
			return ls.remove(n);
		}
		finally
		{
			w.unlock();
		}
	}

	public void setTotalRecordCount(int totalRecordCount)
	{
		w.lock();
		try
		{
			ls.setTotalRecordCount(totalRecordCount);
		}
		finally
		{
			w.unlock();
		}
	}
	
	public int getTotalRecordCount()
	{
		r.lock();
		try
		{
			return ls.getTotalRecordCount();
		}
		finally
		{
			r.unlock();
		}
	}
		
	public boolean contains(E e)
	{
		r.lock();
		try
		{
			return ls.contains(e);
		}
		finally
		{
			r.unlock();
		}
	}

	public E get(int n)
	{
		r.lock();
		try
		{
			return ls.get(n);
		}
		finally
		{
			r.unlock();
		}
	}

	public DataPage<E> getPage(int pageSize, int pageNo)
	{
		r.lock();
		try
		{
			return ls.getPage(pageSize, pageNo);
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
			return ls.isEmpty();
		}
		finally
		{
			r.unlock();
		}
	}

	public int size()
	{
		r.lock();
		try
		{
			return ls.size();
		}
		finally
		{
			r.unlock();
		}
	}
	
	public int maxSize()
	{
		r.lock();
		try
		{
			return ls.maxSize();
		}
		finally
		{
			r.unlock();
		}
	}
	
	public E findElement(E e)
	{
		r.lock();
		try
		{
			return ls.findElement(e);
		}
		finally
		{
			r.unlock();
		}
	}
	
	public ArrayList<E> toArrayList()
	{
		r.lock();
		try
		{
			return ls.toArrayList();
		}
		finally
		{
			r.unlock();
		}
	}
	
	public String toString()
	{
		r.lock();
		try
		{
			return ls.toString();
		}
		finally
		{
			r.unlock();
		}
	}
	
	public static void main(String[] argv)
	{
		ListPageCacheData<String> l = new ListPageCacheData<String>(
				new ArrayList<String>(), 10,6);
		for (int i = 0; i < 6; ++i)
		{
			l.add(i +"");
		}
		DataPage<String> pg = l.getPage(5,2);
		List<String> list = pg.getRecord();
		for(String s : list)
		{
			System.out.println(s);
		}	

	}
}
