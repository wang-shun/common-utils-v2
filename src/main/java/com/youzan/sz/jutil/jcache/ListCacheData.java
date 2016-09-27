package com.youzan.sz.jutil.jcache;

import com.youzan.sz.jutil.data.DataPage;
import com.youzan.sz.jutil.util.CircleListNoSafe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//import com.qq.jutil.data.DataPage;
//import com.qq.jutil.util.CircleListNoSafe;

public final class ListCacheData<E> implements Serializable
{
	private static final long serialVersionUID = 9211273536308396385L;

	private CircleListNoSafe<E> ls;							// 循环队列
	
	private int totalRecordCount;							// 元素总个数（包括不在队列里的元素）
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();	// 锁
	
	private final Lock r = rwLock.readLock();				// 读锁

	private final Lock w = rwLock.writeLock();				// 写锁
	
	/// for hessian
	public ListCacheData()
	{
	}
	
	public void init(List<E> data, int maxSize, int totalRecordCount)
	{
		this.totalRecordCount = totalRecordCount;
		this.ls = new CircleListNoSafe<E>(data, maxSize);
	}
	
	/**
	 * 
	 * @param data 初始化的数据
	 * @param maxSize ListCacheData本身保存的数据的最大记录数
	 * @param totalRecordCount 包括已经淘汰的，总的记录数 --当然，你也可以用来记录其他你想记录的数据
	 */
	public ListCacheData(List<E> data, int maxSize, int totalRecordCount)
	{
		this.totalRecordCount = totalRecordCount;
		this.ls = new CircleListNoSafe<E>(data, maxSize);
	}
	
	public ListCacheData(List<E> data, int maxSize)
	{
		this.ls = new CircleListNoSafe<E>(data, maxSize);
		this.totalRecordCount = ls.size();
	}
	
	public int getTotalRecordCount()
	{
		r.lock();
		try{
			return totalRecordCount;
		}finally{
			r.unlock();
		}
	}
	
	public void setTotalRecordCount(int totalRecordCount)
	{
		w.lock();
		try{
			this.totalRecordCount = totalRecordCount;
		}finally{
			w.unlock();
		}
	}
	
	public DataPage<E> getPage(int pageSize, int pageNo)
	{
		r.lock();
		try{
			pageSize = (pageSize <= 0 ? 10 : pageSize);
			pageNo = (pageNo <= 0 ? 1 : pageNo);
			int begin = pageSize * (pageNo - 1);
			int end = begin + pageSize;
			
			if(end > ls.size())
				end = ls.size();
	
			ArrayList<E> l = new ArrayList<E>();
			for (int i = begin; i < end; ++i)
			{
				E e = get(i);
				l.add(e);
			}
			return new DataPage<E>(l, totalRecordCount, pageSize, pageNo);
		}finally{
			r.unlock();
		}
	}

	public boolean contains(E e)
	{
		r.lock();
		try{
			return ls.contains(e);
		}finally{
			r.unlock();
		}
	}

	public boolean equals(Object arg0)
	{
		r.lock();
		try{
			return ls.equals(arg0);
		}finally{
			r.unlock();
		}
	}

	public E findElement(E e)
	{
		r.lock();
		try{
			return ls.findElement(e);
		}finally{
			r.unlock();
		}
	}

	public E get(int n)
	{
		r.lock();
		try{
			return ls.get(n);
		}finally{
			r.unlock();
		}
	}

	public int hashCode()
	{
		r.lock();
		try{
			return ls.hashCode();
		}finally{
			r.unlock();
		}
	}

	public boolean isEmpty()
	{
		r.lock();
		try{
			return ls.isEmpty();
		}finally{
			r.unlock();
		}
	}

	public int maxSize()
	{
		r.lock();
		try{
			return ls.maxSize();
		}finally{
			r.unlock();
		}
	}

	public void moveTo(E src, int destPos)
	{
		w.lock();
		try{
			ls.moveTo(src, destPos);
		}finally{
			w.unlock();
		}
	}

	public void moveTo(int srcPos, int destPos)
	{
		w.lock();
		try{
			ls.moveTo(srcPos, destPos);
		}finally{
			w.unlock();
		}
	}

	public E moveToTop(E e)
	{
		w.lock();
		try{
			return ls.moveToTop(e);
		}finally{
			w.unlock();
		}
	}

	public void moveToTop(int n)
	{
		w.lock();
		try{
			ls.moveToTop(n);
		}finally{
			w.unlock();
		}
	}

	public E add(E e, boolean changeTotalRecordCount)
	{
		w.lock();
		try{
			if(changeTotalRecordCount)
				++totalRecordCount;
			return ls.add(e);
		}finally{
			w.unlock();
		}
	}

	public E add(int n, E e, boolean changeTotalRecordCount)
	{
		w.lock();
		try{
			if(changeTotalRecordCount)
				++totalRecordCount;
			return ls.add(n, e);
		}finally{
			w.unlock();
		}
	}
		
	public E remove(E e, boolean changeTotalRecordCount)
	{
		w.lock();
		try{
			E o = ls.remove(e);
			if(changeTotalRecordCount && o != null)
				--totalRecordCount;
			return o;
		}finally{
			w.unlock();
		}
	}

	public E remove(int n, boolean changeTotalRecordCount)
	{
		w.lock();
		try{
			E o = ls.remove(n);
			if(changeTotalRecordCount && o != null)
				--totalRecordCount;
			return o;
		}finally{
			w.unlock();
		}
	}
/*
	public void clear()
	{
		w.lock();
		try{
			ls.clear();
		}finally{
			w.unlock();
		}
	}
*/
	public int size()
	{
		r.lock();
		try{
			return ls.size();
		}finally{
			r.unlock();
		}
	}

	public ArrayList<E> toArrayList()
	{
		r.lock();
		try{
			return ls.toArrayList();
		}finally{
			r.unlock();
		}
	}

	public String toString()
	{
		r.lock();
		try{
			return "{totalRecordCount: " + totalRecordCount + ", ls: " + ls.toString() + "}";
		}finally{
			r.unlock();
		}
	}
	
	public static void main(String[] argv)
	{
		ArrayList<String> ls = new ArrayList<String>();
		ls.add("sss");
		ls.add("ss1");
		ls.add("ss2");
		ls.add("ss3");
		ListCacheData<String> l = new ListCacheData<String>(ls, 2, ls.size());
		l.add("hello", true);
		
		System.out.println(l.size());
		System.out.println(l.getPage(5, 1));
	}
}
