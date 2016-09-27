package com.youzan.sz.jutil.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 循环队列
 * 
 * @author meteor 用于缓存列表页数据，只缓存前面的n条数据
 * @param <E>
 */
@SuppressWarnings("serial")
public final class CircleListNoSafe<E> implements Serializable
{
	private ArrayList<E> ls;	// 数据列表

	private int topIdx;			// 队列头位置

	private int size;			// 队列大小
	
	public CircleListNoSafe()
	{
	}

	public CircleListNoSafe(List<E> data)
	{
		init(data);
	}

	public void init(List<E> data)
	{
		size = data.size();
		if(size > 0)
			topIdx = size - 1;
		ls = new ArrayList<E>(size);
		//ls.addAll(data);
		for(int i = data.size() - 1; i >= 0; --i)
			ls.add(data.get(i));
	}

	public CircleListNoSafe(List<E> data, int maxSize)
	{
		if(data.size() > maxSize)
		{
			//List<E> l = data.subList(data.size() - maxSize, data.size());
			List<E> l = data.subList(0, maxSize);
			init(l);
		}
		else if(data.size() == maxSize)
		{
			init(data);
		}
		else
		{
			size = data.size();
			if(size > 0)
				topIdx = size - 1;
			ls = new ArrayList<E>(maxSize);//这里的只是初始为一个初始容量是maxSize的list,其size为是0
			//构建一个size为maxSize的list
			for(int i = data.size() - 1; i >= 0; --i)
				ls.add(data.get(i));
			for (int i = 0; i < maxSize - size; i++) 
			{
				ls.add(null);
			}
		}
	}

	/**
	 * 获取缓存的元素个数
	 * 
	 * @return
	 */
	public int size()
	{
		return size;
	}
	
	public int maxSize()
	{
		return ls.size();
	}

	/**
	 * 获取第n个元素
	 * 
	 * @param n
	 *            元素的下标
	 * @return
	 */
	public E get(int n)
	{
		if (n < 0 || n >= size)
			throw new IndexOutOfBoundsException("current index: " + n
					+ ", size: " + size);
		int idx = getIdx(n);
		return ls.get(idx);
	}

	/**
	 * 往前面添加元素，如果超过最大大小，则把最早加入的元素删掉
	 * 
	 * @param e
	 *            要添加的元素
	 * @return	被删掉的元素
	 */
	public E add(E e)
	{
		topIdx = (topIdx + 1) % ls.size();
		if (size < ls.size())
			++size;
		return ls.set(topIdx, e);
	}

	public E add(int n, E e)
	{
		topIdx = (topIdx + 1) % ls.size();
		if (size < ls.size())
			++size;
		int i = 1;
		for (; i <= n; ++i)
		{
			E o = get(i);
			int idx = getIdx(i - 1);
			ls.set(idx, o);
		}
		int idx = getIdx(i - 1);
		return ls.set(idx, e);
	}

	public boolean isEmpty()
	{
		return size == 0;
	}

	/**
	 * 判断缓存中是否包含元素e
	 * @param e
	 * @return
	 */
	public boolean contains(E e)
	{
		for (int i = 0; i < size; ++i)
		{
			E o = get(i);
			if ((e == null && o == null) || (e != null && e.equals(o)))
				return true;
		}
		return false;
	}

	/**
	 * 删除一个元素
	 * 
	 * @param n
	 *            元素的下标
	 */
	public E remove(int n)
	{
		if (n < 0 || n >= size)
			throw new IndexOutOfBoundsException("current index: " + n
					+ ", size: " + size);
		E o = get(n);
		int i = n + 1;
		for (; i < size; ++i)
		{
			int idx1 = getIdx(i - 1);
			int idx2 = getIdx(i);
			ls.set(idx1, ls.get(idx2));
		}
		int idx = getIdx(--size);
		ls.set(idx, null);
		//--size;
		return o;
	}

	/**
	 * 删除元素
	 * @param e
	 */
	public E remove(E e)
	{
		for (int i = 0; i < size; ++i)
		{
			E o = get(i);
			if ((e == null && o == null) || (e != null && e.equals(o)))
			{
				return remove(i);
			}
		}
		return null;
	}

	private int getIdx(int i)
	{
		int idx = topIdx - i;
		idx = idx < 0 ? idx + ls.size() : idx;
		return idx;
	}

	/**
	 * 将第n个元素提前到顶端
	 * 
	 * @param n
	 *            元素的下标
	 */
	public void moveToTop(int n)
	{
		E o = get(n);
		remove(n);
		add(o);
	}

	/**
	 * 从表中查找第一个元素e，并将之提前到顶端
	 * 
	 * @param e
	 */
	public E moveToTop(E e)
	{
		for (int i = 0; i < size; ++i)
		{
			E o = get(i);
			if ((e == null && o == null) || (e != null && e.equals(o)))
			{
				remove(i);
				add(o);
				return null;
			}
		}
		return add(e);
	}

	/**
	 * 将元素移动到指定位置
	 * @param srcPos	原始元素的位置
	 * @param destPos	目的位置
	 */
	public void moveTo(int srcPos, int destPos)
	{
		if(srcPos <= destPos && destPos > 0)
			--destPos;
		E o = remove(srcPos);
		if(o != null)
			add(destPos, o);
	}
	
	/**
	 * 将元素移动到指定位置
	 * @param src		元素
	 * @param destPos	目的位置
	 */
	public void moveTo(E src, int destPos)
	{
		int srcPos = find(src);
		if(srcPos != -1)
			moveTo(srcPos, destPos);
		else
			add(destPos, src);
		/*
		E o = remove(src);
		if(o != null)
			add(destPos, o);
			*/
	}

	/**
	 * 清除所有元素
	 *
	 */
	public void clear()
	{
		size = 0;
	}

	public ArrayList<E> toArrayList()
	{
		ArrayList<E> l = new ArrayList<E>();
		l.ensureCapacity(size);
		for(int i = 0; i < size; ++i)
			l.add(get(i));
		return l;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ data: [");
		for(int i = 0; i < size; ++i)
		{
			sb.append(String.valueOf(get(i)));
			if(i != size)
				sb.append(", ");
		}
		sb.append("], size: ")
			.append(size)
			.append(" }");
		return sb.toString();
	}
	
	private int find(E e)
	{
		for (int i = 0; i < size; ++i)
		{
			E o = get(i);
			if ((e == null && o == null) || (e != null && e.equals(o)))
			{
				return i;
			}
		}
		return -1;
	}
	
	public E findElement(E e)
	{
		for (int i = 0; i < size; ++i)
		{
			E o = get(i);
			if ((e == null && o == null) || (e != null && e.equals(o)))
			{
				return o;
			}
		}
		return null;
	}

	public static void main(String[] argv)
	{
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("1");
		ll.add("2");
		ll.add("3");
		CircleListNoSafe<String> l = new CircleListNoSafe<String>(
				ll, 2);
		for(int i = 0; i < l.size(); ++i){
			System.out.println(l.get(i));
		}
		System.out.println(Arrays.toString(l.toArrayList().toArray()));
		/*
		ListPageCacheDataImpl<String> l = new ListPageCacheDataImpl<String>(
				new ArrayList<String>(), 10, 100);
		for (int i = 0; i < 30; ++i)
		{
			l.add("" + i);
		}
		l.moveToTop(4);
		for (int i = 0; i < l.size(); ++i)
		{
			System.out.println(l.get(i));
		}
		*/
	}
}
