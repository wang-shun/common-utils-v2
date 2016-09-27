package com.youzan.sz.jutil.util;

import com.youzan.sz.jutil.j4log.Logger;

import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

//import com.qq.jutil.j4log.Logger;

public final class TimeoutDetector<T>
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	public static class Info<V> implements Comparable<Info<V>>
	{
		V		v;
		long	expireTime;
		
		Info(V v, long expireTime)
		{
			this.v = v;
			this.expireTime = expireTime;
		}

		public int compareTo(Info<V> o)
		{
			return expireTime > o.expireTime ? 1 : (expireTime < o.expireTime ? -1 : 0);
		}
	}
	
	private PriorityBlockingQueue<Info<T>>[] qs;
	private int		idx;
	private Thread	t;
	
	public TimeoutDetector() {
		this(11);
	}
	
	@SuppressWarnings("unchecked")
	public TimeoutDetector(int queueCount) {
		qs = new PriorityBlockingQueue[queueCount];
		for (int i = 0; i < qs.length; ++i)
			qs[i] = new PriorityBlockingQueue<Info<T>>();
	}
	
	public void addWithExpire(T obj, long expireTime)
	{
		Info<T> o = new Info<T>(obj, expireTime);
		idx = (idx + 1) % qs.length;
		qs[idx].add(o);
	}
	
	public void addWithTimeout(T obj, long timeout)
	{
		addWithExpire(obj, System.currentTimeMillis() + timeout);
	}

	public T poll()
	{
		return poll(System.currentTimeMillis());
	}
	
	public T poll(long now)
	{
		for (PriorityBlockingQueue<Info<T>> q : qs) {
			Info<T> v = q.poll();
			if(v != null) {
				if(v.expireTime <= now)
					return v.v;
				q.add(v);
			}
		}
		return null;
	}
	
	public Info<T> pollInfo(long now) {
		for (PriorityBlockingQueue<Info<T>> q : qs) {
			Info<T> info = pollInfo(q, now);
			if(info != null)
				return info;
		}
		return null;
	}
	
	private Info<T> pollInfo(PriorityBlockingQueue<Info<T>> q, long now)
	{
		Info<T> v = q.poll();
		if(v == null)
			return null;
		if(v.expireTime <= now)
			return v;
		q.add(v);
		return null;
	}

	public int size()
	{
		int n = 0;
		for (PriorityBlockingQueue<Info<T>> q : qs)
			n += q.size();
		return n;
	}

	public boolean isEmpty()
	{
		for (PriorityBlockingQueue<Info<T>> q : qs) {
			if (!q.isEmpty())
				return false;
		}
		return true;
	}
	
	public void clear()
	{
		for (PriorityBlockingQueue<Info<T>> q : qs)
			q.clear();
	}
	
	public boolean startHandleThread(final TimeoutHandler<T> h)
	{
		return startHandleThread(h, 500);
	}
	
	public boolean startHandleThread(final TimeoutHandler<T> h, final int checkInterval)
	{
		if(t != null)
			return false;
		t = new Thread(new Runnable(){
			public void run()
			{
				while(true){
					try{
						long now = System.currentTimeMillis();
						for (PriorityBlockingQueue<Info<T>> q : qs) {
							Info<T> v = q.peek();
							// 先检查第一个元素是否已过期，若是，则循环处理，否则直接忽略
							if(v != null && v.expireTime <= now){
								while(true){
									v = pollInfo(q, now);
									if(v == null || v.expireTime > now)
										break;
									try {
										h.handle(v.v, v.expireTime);
									} catch (Throwable e) {
										logger.error("timeout handler exception: ", e);
									}
								}
							}
						}
						Thread.sleep(checkInterval);
					}catch(Throwable e){
						logger.error("TimeoutDetector.startHandleThread: ", e);
					}
				}
			}
		});
		t.start();
		return true;
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		final long base = System.currentTimeMillis();
		System.out.println("starting...");
		TimeoutDetector<String> td = new TimeoutDetector<String>();
		td.startHandleThread(new TimeoutHandler<String>(){
			public void handle(String o, long expireTime)
			{
				long now = System.currentTimeMillis() - base;
				long e = expireTime - base;
				System.out.println("s: " + o + ", expireTime: " + e + ", now: " + now + ", x: " + (now - e));
			}
		});
		long now = System.currentTimeMillis();
		td.addWithTimeout("2.5 second", 2500);
		td.addWithExpire("1 second", now + 1000);
		td.addWithTimeout("2 second", 2000);
		td.addWithExpire("expired", now - 100);
		td.addWithTimeout("5 second", 5000);
		Random rnd = new Random();
		for(int i = 0; i < 20; ++i){
			int to = rnd.nextInt(10000);
			td.addWithTimeout("" + to + " ms", to);
			Thread.sleep(1000);
		}
		System.out.println("ending...");
		Thread.sleep(100000000);
	}
}
