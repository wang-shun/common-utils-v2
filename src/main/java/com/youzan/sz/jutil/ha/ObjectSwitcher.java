package com.youzan.sz.jutil.ha;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectSwitcher<E> implements Switcher
{
	private E o1;
	private E o2;
	private final Lock l = new ReentrantLock();
	
	public ObjectSwitcher(E o1, E o2)
	{
		this.o1 = o1;
		this.o2 = o2;
	}
	
	public void reset(E o1, E o2)
	{
		this.o1 = o1;
		this.o2 = o2;
	}
	
	public E get()
	{
		return o1;
	}
	
	public boolean doSwitch()
	{
		if(!l.tryLock())
			return false;
		try{
			E o3 = o1;
			o1 = o2;
			o2 = o3;
		}finally{
			l.unlock();
		}
		return true;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{ObjectSwitcher, o1: ")
			.append(o1)
			.append(", o2: ")
			.append(o2)
			.append("}");
		return sb.toString();
	}
}
