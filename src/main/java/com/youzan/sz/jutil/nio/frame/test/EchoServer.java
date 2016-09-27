package com.youzan.sz.jutil.nio.frame.test;

import com.youzan.sz.jutil.nio.frame.ClientReader;
import com.youzan.sz.jutil.nio.frame.ClientWriter;

public final class EchoServer
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("---server start---");
		ClientReader cr = new ClientReader("0.0.0.0", 33306, new EchoTaskFactory());
		EchoHandler dh = new EchoHandler();
		ClientWriter cw = new ClientWriter();
		cr.setNext(dh);
		dh.setNext(cw);
		cw.setNext(cr);
		
		try
		{
			cr.startServer();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
