package com.youzan.sz.jutil.net.cap;

import java.net.InetSocketAddress;

/**
 * 数据包
 * @author junehuang
 *
 */
public interface CapPacket {
	InetSocketAddress getAddress();
	int getSize();
	int getSeq();
	
	/**
	 * 返回了整个数据包，包括包头，包体，包尾
	 * @return
	 */
    byte[] getPacket();
}

/**
 * 实现类，不public
 * @author junehuang
 *
 */
class CapPacketImpl implements CapPacket{
	InetSocketAddress addr;
	int size;
	int seq;
	byte[] packet;
	
	public CapPacketImpl(InetSocketAddress addr, int size, int seq, byte[] packet){
		this.addr = addr;
		this.size = size;
		this.seq = seq;
		this.packet = packet;
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
	public byte[] getPacket(){
		return packet;
	}
}
