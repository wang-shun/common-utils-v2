package com.youzan.sz.jutil.net.simple;

import java.io.IOException;

/**
 * 这里就是封装了一个网络连接
 * @author junehuang
 *
 */
public interface Session {
	/**
	 * 常量定义：session的三种状态：未连接，已连接，已经关闭
	 * @author huangjun
	 *
	 */
	public static enum SessionStatus{
		NOT_CONNECTED,//未连接，这是只有客户端才有的状态
		CONNECTED,
		CLOSED
	}
	
	/**
	 * 获取session的当前状态
	 * @return
	 */
	SessionStatus getState();
	
	/**
	 * 异步写入，如果是NOT_CONNECTED不会抛出异常。所以在调用此方法之前最好先调用getState()判断一下
	 * @param seq 序列号
	 * @param bs
	 * @return 是否写入了待发队列
	 * @throws 可能session的状态是已经关闭。或者写队列满了，或者bs长度超过最大限度
	 */
	void write(int seq, byte[] bs) throws IOException;
	
	/**
	 * 立即关闭连接，如果有没有发完的包会丢弃
	 */
	public void close();
}
