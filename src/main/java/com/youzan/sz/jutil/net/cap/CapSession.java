package com.youzan.sz.jutil.net.cap;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * 这里就是封装了一个网络连接
 * @author junehuang
 *
 */
public interface CapSession {
	/**
	 * 常量定义：session的三种状态：未连接，已连接，已经关闭
	 * @author junehuang
	 *
	 */
	public static enum CapSessionStatus{
		NOT_CONNECTED,//未连接，这是只有客户端才有的状态
		CONNECTED,
		CLOSED
	}
	
	/**
	 * 获取session的当前状态
	 * @return
	 */
	CapSessionStatus getState();
	
	/**
	 * 异步写入，如果是NOT_CONNECTED不会抛出异常。所以在调用此方法之前最好先调用getState()判断一下
	 * @param seq 序列号
	 * @param packet 包括包头包尾在内的所有字节
	 * @throws 可能session的状态是已经关闭。或者写队列满了，或者bs长度超过最大限度
	 */
	void write(int seq, byte[] packet) throws IOException;
	
	/**
	 * 立即关闭连接，如果有没有发完的包会丢弃
	 */
	public void close();

	/**
	 * 获取远程socketAddr
	 * @return
	 */
	public InetSocketAddress getRemoteSocketAddress() throws IOException;

	/**
	 * 仅用于服务器端获取远程客户端连接过来的socket的描述，在连接建立的时候会设置这个描述信息，格式IP:PORT
	 * @return
	 */
	public String getRemoteSocketDesc();
	
}
