package com.youzan.sz.jutil.net;
import java.net.InetAddress;

/**
 * 网络模块传递给应用模块的请求封装
 * @author june
 *
 */
public interface RequestObj {
	/**
	 * 顾名思义
	 * @return
	 */
	public InetAddress getAddress();
	/**
	 * 顾名思义
	 * @return
	 */
	public int getPort();
	/**
	 * 本次网络请求的数据，不包括网络控制部分的包头
	 * @return
	 */
	public byte[] getData();
	/**
	 * 请求的序列号，这个属于网络控制部分，似乎不应该传递给应用，但是有时候确实用得着。
	 * @return
	 */
	public int getSeq();
}
