package com.youzan.sz.jutil.nio.frame.test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.youzan.sz.jutil.nio.frame.Task;

final class EchoTask implements Task
{
	private SocketChannel sc;
	
	private ByteBuffer writeBuf;
	
	public EchoTask(SocketChannel sc)
	{
		this.sc = sc;
	}
	
	public SocketChannel getChannel()
	{
		return sc;
	}
	
	public boolean isReadFinish()
	{
		return true;
	}
	
	public int readFromClient()
	{
		try{
			ByteBuffer bb = ByteBuffer.allocate(1024);
			int n = sc.read(bb);
			if (n <= 0)
				return n;
			byte[] bs = bb.array();
			writeBuf = ByteBuffer.allocate(bb.position());
			writeBuf.put(bs, 0, bb.position());
			writeBuf.flip();
			return n;
		}catch(Exception e){
			return -1;
		}
	}
	
	public boolean isWriteFinish()
	{
		return writeBuf.remaining() <= 0;
	}
	
	public int writeToClient()
	{
		try
		{
			return sc.write(writeBuf);
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	public void reset()
	{
		writeBuf.clear();
	}
}
