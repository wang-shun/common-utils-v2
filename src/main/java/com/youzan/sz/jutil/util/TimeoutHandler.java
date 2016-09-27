package com.youzan.sz.jutil.util;

public interface TimeoutHandler<T>
{
	public void handle(T o, long expireTime);
}
