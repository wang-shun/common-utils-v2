package com.youzan.sz.jutil.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 线程安全的循环队列
 * @author meteorchen
 *
 * @param <E>
 */
public final class CircleList<E>
{
	private CircleListNoSafe<E> ls;			// 循环队列
	
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();	// 读写锁

	private final Lock r = rwLock.readLock();	// 读锁

	private final Lock w = rwLock.writeLock();	// 写锁

	public CircleList()
	{
		ls = new CircleListNoSafe<E>();
	}
	
	public CircleList(List<E> data)
	{
		ls = new CircleListNoSafe<E>(data);
	}
	
	public CircleList(List<E> data, int maxSize)
	{
		ls = new CircleListNoSafe<E>(data, maxSize);
	}
	
	public void init(List<E> data)
	{
		ls.init(data);
	}
	
	public E add(E e)
	{
		w.lock();
		try{
			return ls.add(e);
		}finally{
			w.unlock();
		}
	}

	public E add(int n, E e)
	{
		w.lock();
		try{
			return ls.add(n, e);
		}finally{
			w.unlock();
		}
	}

	public void clear()
	{
		w.lock();
		try{
			ls.clear();
		}finally{
			w.unlock();
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

	public E remove(E e)
	{
		w.lock();
		try{
			return ls.remove(e);
		}finally{
			w.unlock();
		}
	}

	public E remove(int n)
	{
		w.lock();
		try{
			return ls.remove(n);
		}finally{
			w.unlock();
		}
	}

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
			return ls.toString();
		}finally{
			r.unlock();
		}
	}
}
