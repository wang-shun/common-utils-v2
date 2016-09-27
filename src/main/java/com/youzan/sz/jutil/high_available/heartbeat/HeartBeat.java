package com.youzan.sz.jutil.high_available.heartbeat;

import com.youzan.sz.jutil.util.Pair;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

//import com.qq.jutil.util.Pair;

public final class HeartBeat implements Runnable
{
	private int timeout = 20000;		// 超时时间
	private int interval = 5000;		// 心跳间隔
	private InetAddress remoteAddress;	// 远程机器地址
	private int remotePort;				// 远程机器端口
	private int localPort;				// 本地端口
	
	private DatagramSocket dsReceive;	// 接收报文
	private DatagramSocket dsSend;		// 发送报文
	
	private int remoteState = STATE_ALIVE;	// 远程机器状态
	private int localState = STATE_ALIVE;	// 本机状态
	private int localState2 = STATE_ALIVE;	// 远程机器上的本机状态
	
	private HostEvent notify;			// 事件通知接口
	
	public static final int STATE_ALIVE = 0;
	public static final int STATE_DEAD = -1;
	//private static final byte[] HELLO_PACKET = "hello".getBytes();
	//private byte[] helloPacket = "".getBytes();
	
	/**
	 * 构造方法
	 * @param remoteAddress				远程机器的IP地址
	 * @param remotePort				远程机器的端口
	 * @param localPort					本地接收心跳信息的端口
	 * @param notify					状态变化通知接口
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public HeartBeat(String remoteAddress, int remotePort, int localPort, HostEvent notify) throws UnknownHostException, SocketException
	{
		this(remoteAddress, remotePort, localPort, notify, 5000, 20000);
	}
	
	/**
	 * 构造方法
	 * @param remoteAddress				远程机器的IP地址
	 * @param remotePort				远程机器的端口
	 * @param localPort					本地接收心跳信息的端口
	 * @param notify					状态变化通知接口
	 * @param interval					检查间隔
	 * @param timeout					超时时间
	 * @throws UnknownHostException
	 * @throws SocketException
	 */
	public HeartBeat(String remoteAddress, int remotePort, int localPort, HostEvent notify, int interval, int timeout) throws UnknownHostException, SocketException
	{
		this.interval = interval;
		this.timeout = timeout;
		this.notify = notify;
		this.remoteAddress = InetAddress.getByName(remoteAddress);
		this.remotePort = remotePort;
		this.localPort = localPort;
		this.dsReceive = new DatagramSocket(localPort);
		this.dsReceive.setSoTimeout(interval);
		this.dsSend = new DatagramSocket();
		this.dsSend.connect(this.remoteAddress, remotePort);
		//this.helloPacket = toBytes(localState);
	}
	
	/**
	 * 设置本地机器的状态
	 * @param state		新状态
	 */
	public void setLocalState(int state)
	{
		this.localState = state;
		//this.helloPacket = toBytes(localState);
		send();
	}
	
	private static byte[] toBytes(int localState, int remoteState)
	{
		ByteBuffer bb = ByteBuffer.allocate(8);
		bb.putInt(localState);
		bb.putInt(remoteState);
		return bb.array();
	}
	
	/**
	 * 获取远程机器的地址
	 * @return	远程机器地址
	 */
	public InetAddress getRemoteAddress()
	{
		return this.remoteAddress;
	}
	
	/**
	 * 获取远程机器的端口
	 * @return	远程机器端口
	 */
	public int getRemotePort()
	{
		return this.remotePort;
	}
	
	/**
	 * 获取本地接收心跳信息的端口
	 * @return	本地端口
	 */
	public int getLocalPort()
	{
		return this.localPort;
	}
	
	/**
	 * 获取远程机器当前的状态
	 * @return	远程机器状态
	 */
	public int getRemoteState()
	{
		return remoteState;
	}

	public boolean isLocalStateConfirm()
	{
		return this.localState == this.localState2;
	}

	/**
	 * 判断远程机器是否已停机
	 * @return	远程机器是否已停机
	 */
	public boolean isRemoteDead()
	{
		return remoteState == STATE_DEAD;
	}
	
	private void send()
	{
		try{
			//System.out.println("---send hello---" + new Date());
			//DatagramPacket outgoing = new DatagramPacket(helloPacket, helloPacket.length, dsSend.getInetAddress(), dsSend.getPort());
			byte[] bs = toBytes(localState, remoteState);
			DatagramPacket outgoing = new DatagramPacket(bs, bs.length, dsSend.getInetAddress(), dsSend.getPort());
			dsSend.send(outgoing);
		}catch(Exception e){
		}
	}
	
	private Pair<Integer, Integer> receive()
	{
        try{
    		byte[] buffer = new byte[8];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            dsReceive.receive(incoming);
            ByteBuffer bb = ByteBuffer.wrap(buffer);
        	//System.out.println("---receive hello---" + new Date());
        	int newRemoteState = bb.getInt();
        	int newLocalState2 = bb.getInt();
        	return Pair.makePair(newRemoteState, newLocalState2);
        }catch(Exception e){
        	return Pair.makePair(-1, -1);
        }
	}
	
	private void onStateChange(int newState, int oldState)
	{
		remoteState = newState;
		notify.onStateChange(newState, oldState);
	}
	
	/**
	 * 启动心跳
	 */
	public void run()
	{
		long lastReceive = System.currentTimeMillis();
		while(true){
			try{
				long now = System.currentTimeMillis();
				send();
				Pair<Integer, Integer> pr = receive();
				int newState = pr.first;
				if(newState != STATE_DEAD){
					if(newState != remoteState)
						onStateChange(newState, remoteState);
					lastReceive = System.currentTimeMillis();
				}else if(remoteState != STATE_DEAD && System.currentTimeMillis() - lastReceive > timeout){
					onStateChange(STATE_DEAD, remoteState);
				}
				long t = interval - (System.currentTimeMillis() - now);
				if(t > 0){
					Thread.sleep(t);
				}
				if(pr.second != STATE_DEAD && pr.second != this.localState2){
					notify.onLocalStateNotify(pr.second, this.localState2);
					this.localState2 = pr.second;
				}
			}catch(Throwable e){
				//e.printStackTrace();
			}
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{remoteAddress: ")
			.append(remoteAddress)
			.append(", ")
			.append("remotePort: ")
			.append(remotePort)
			.append(", ")
			.append("localPort: ")
			.append(localPort)
			.append(", ")
			.append("state: ")
			.append(remoteState)
			.append(", ")
			.append("interval: ")
			.append(interval)
			.append(", ")
			.append("timeout: ")
			.append(timeout)
			.append(", ")
			.append("}");
		return sb.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

	}

}
