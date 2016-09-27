package com.youzan.sz.jutil.sso;

import java.nio.ByteBuffer;

public class SsoHead {
	private boolean heartbeat;//内部变量，不序列化,用于判断是否是心跳包

	private int clientIp;
	private int clientPort;
	private int clientIPInfo;
	private int clientID;

	public int getClientIp() {
		return clientIp;
	}

	public void setClientIp(int clientIp) {
		this.clientIp = clientIp;
	}

	public int getClientPort() {
		return clientPort;
	}

	public void setClientPort(int clientPort) {
		this.clientPort = clientPort;
	}

	public int getClientIPInfo() {
		return clientIPInfo;
	}

	public void setClientIPInfo(int clientIPInfo) {
		this.clientIPInfo = clientIPInfo;
	}

	public int getClientID() {
		return clientID;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}
	
	public boolean isHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(boolean heartbeat) {
		this.heartbeat = heartbeat;
	}	
	
	public byte[] encode(){
		ByteBuffer buf = ByteBuffer.allocate(20);
		buf.putInt(20);//SsoHeadLen
		buf.putInt(clientIp);//ClientIP
		buf.putInt(clientPort);//Port
		buf.putInt(clientIPInfo);//clientIPInfo
		buf.putInt(clientID);//ClientID
		return buf.array();
	}
	
	public static SsoHead decode(ByteBuffer buf){
		int defaultLen = 20;
		SsoHead sh = new SsoHead();
		int sshHeadLen = buf.getInt();
		if(sshHeadLen == 4){
			sh.setHeartbeat(true);
		}
		else{
			sh.setClientIp(buf.getInt());
			sh.setClientPort(buf.getInt());
			sh.setClientIPInfo(buf.getInt());
			sh.setClientID(buf.getInt());
			
			//为了扩展，跳过可能扩充的字段
			if(sshHeadLen > defaultLen){
				buf.position(buf.position() + sshHeadLen  - defaultLen);
			}
		}
		return sh;
	}

}
