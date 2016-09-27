package com.youzan.sz.jutil.thread;

import com.youzan.sz.jutil.j4log.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.Logger;

/**
 * 固定大小的线程池
 * @author meteorchen
 *
 */
public final class FixThreadExecutor implements Executor
{
	private ThreadWrapper[] threads;			// 线程数组
	
	private BlockingQueue<Runnable> tasks;// = new ArrayBlockingQueue<Runnable>();	// 任务队列
	
	private int maxTask;						// 最大任务数
	
	private volatile boolean stopFlag = false;	// 是否已关闭
	
	private final int maxAliveTime;				// 线程的最大生存时间
	
	private final int maxUseCount;				// 线程的最大执行任务次数
	
	private final AtomicInteger activeCount = new AtomicInteger(0);	// 当前激活线程数
	
	private static final Logger debugLog = Logger.getLogger("jutil");
	
	public FixThreadExecutor(int threadNum, int maxTask)
	{
		this(threadNum, maxTask, 24 * 60 * 60 * 1000, 100000);
	}
		
	public FixThreadExecutor(int threadNum, int maxTask, final int maxAliveTime, final int maxUseCount)
	{
		this.maxAliveTime = maxAliveTime;
		this.maxUseCount = maxUseCount;
		threads = new ThreadWrapper[threadNum];
		this.maxTask = maxTask;
		this.tasks = new ArrayBlockingQueue<Runnable>(maxTask);
		for(int i = 0; i < threads.length; ++i){
			threads[i] = new ThreadWrapper(new Proc(i));
			threads[i].start();
		}
	}
	
	private final class Proc implements Runnable
	{
		private int idx;
		
		public Proc(int idx)
		{
			this.idx = idx;
		}
		
		private boolean isValid(ThreadWrapper t)
		{
			long now = System.currentTimeMillis();
			if (now - t.createTime > maxAliveTime)
				return false;
			return t.useCount <= maxUseCount;
		}
		
		public void run()
		{
			while(true){
				if(stopFlag)
					break;
				try{
					if(!isValid(threads[idx]))
					{
						threads[idx] = new ThreadWrapper(new Proc(idx));
						threads[idx].start();
						return;
					}
					Runnable r = tasks.poll(10, TimeUnit.SECONDS);
					if(r != null){
						activeCount.incrementAndGet();
						try{
							r.run();
						}finally{
							activeCount.decrementAndGet();
						}
					}
					++threads[idx].useCount;
				}catch(Throwable e){
					debugLog.error("FixThreadExecutor.run error: ", e);
				}
			}
		}
	}
	
	public void stop()
	{
		tasks.clear();
		stopFlag = true;
	}
	
	public int getTaskCount()
	{
		return tasks.size();
	}
	
	public int getThreadCount()
	{
		return threads.length;
	}
	
	public int getMaxTaskCount()
	{
		return maxTask;
	}

	public void execute(Runnable r)
	{
		if(tasks.size() >= this.maxTask)
			throw new RuntimeException("task queue is full, current length: " + tasks.size());
		boolean succ = false;
		int i = 0;
		while(!succ){
			try
			{
				tasks.put(r);
				succ = true;
			}
			catch (InterruptedException e)
			{
				debugLog.error("FixThreadExecutor.execute error: ", e);
				if(++i >= 10)
					throw new RuntimeException("put to task queue fail: " + e);
			}
		}
	}
	
	public int getActiveThreadCount()
	{
		return activeCount.get();
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("taskCount: ").append(getTaskCount())
			.append(", threadCount: ").append(getThreadCount())
			.append(", maxTaskCount: ").append(getMaxTaskCount())
			.append(", maxAliveTime: ").append(maxAliveTime)
			.append(", maxUseCount: ").append(maxUseCount)
			.append("}");
		return sb.toString();
	}
	
	public void waitForFinish()
	{
		final long t = 1000;
		while(!tasks.isEmpty() || activeCount.get() > 0){
			try{
				Thread.sleep(t);
			}catch(Throwable e){
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		FixThreadExecutor t = new FixThreadExecutor(10, 10000, 10 * 1000, 30);
		for(int i = 0; i < 1000; ++i)
		{
			t.execute(new Runnable(){
				public void run()
				{
					long id = Thread.currentThread().getId();
					System.out.println("id: " + id);
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
			});
		}
	}

}
