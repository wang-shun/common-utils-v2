package com.youzan.sz.jutil.net.simple;

import java.net.InetSocketAddress;

/**
 * 数据包，不管是请求还是应答都是同样的格式：size(4)+seq(4)+body。
 * @author huangjun
 *
 */
public interface Packet {
	InetSocketAddress getAddress();
	int getSize();
	int getSeq();
	
	/**
	 * 返回了一个buffer，至于pos=0或者limit=capacity都不保证。
	 * @return
	 */
    byte[] getBody();
    
}

/**
 * 实现类，不public
 * @author junehuang
 *
 */
class PacketImpl implements Packet{
	InetSocketAddress addr;
	int size;
	int seq;
	byte[] body;
	
	public PacketImpl(InetSocketAddress addr, int size, int seq, byte[] body){
		this.addr = addr;
		this.size = size;
		this.seq = seq;
		this.body = body;
	}
	public InetSocketAddress getAddress(){
		return addr;
	}
	public int getSize(){
		return size;
	}
	public int getSeq(){
		return seq;
	}
	public byte[] getBody(){
		return body;
	}
}
