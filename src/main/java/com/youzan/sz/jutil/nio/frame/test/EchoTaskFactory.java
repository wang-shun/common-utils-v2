package com.youzan.sz.jutil.nio.frame.test;

import java.nio.channels.SocketChannel;

import com.youzan.sz.jutil.nio.frame.Task;
import com.youzan.sz.jutil.nio.frame.TaskFactory;

final class EchoTaskFactory implements TaskFactory
{
	public Task createTask(SocketChannel sc)
	{
		return new EchoTask(sc);
	}
	/*
	public static void destoryTask(Task task)
	{
	}
	*/
}
