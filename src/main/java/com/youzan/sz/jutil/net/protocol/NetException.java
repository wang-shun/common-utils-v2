package com.youzan.sz.jutil.net.protocol;

@SuppressWarnings("serial")
public class NetException extends RuntimeException
{
	public NetException(String message)
	{
		super(message);
	}
	
	public NetException(String message, Throwable cause)
	{
		super(message, cause);
	}	
}
