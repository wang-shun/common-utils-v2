package com.youzan.sz.jutil.thread;

final class ThreadWrapper
{
	Thread thread;									// 线程
	int useCount = 0;								// 执行任务次数
	long createTime = System.currentTimeMillis();	// 创建时间
	
	public ThreadWrapper(Runnable run)
	{
		thread = new Thread(run);
	}
	
	public void start()
	{
		thread.start();
	}
}
