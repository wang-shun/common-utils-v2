package com.youzan.sz.jutil.sso;

import java.nio.ByteBuffer;

public class ReqHead {
	private int seqNo;
	private byte[] uin;
	private int bid;
	private byte[] lc = new byte[16];
	private byte[] A2;
	private byte[] serviceCmd;
	private byte[] cookie;
	private byte uinType;
	private int errorCode;
	private byte[] imei;

	public int getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(int seqNo) {
		this.seqNo = seqNo;
	}

	public byte[] getUin() {
		return uin;
	}

	public void setUin(byte[] uin) {
		this.uin = uin;
	}

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public byte[] getLc() {
		return lc;
	}

	public void setLc(byte[] lc) {
		this.lc = lc;
	}

	public byte[] getA2() {
		return A2;
	}

	public void setA2(byte[] a2) {
		A2 = a2;
	}

	public byte[] getServiceCmd() {
		return serviceCmd;
	}

	public void setServiceCmd(byte[] serviceCmd) {
		this.serviceCmd = serviceCmd;
	}

	public byte[] getCookie() {
		return cookie;
	}

	public void setCookie(byte[] cookie) {
		this.cookie = cookie;
	}

	public byte getUinType() {
		return uinType;
	}

	public void setUinType(byte uinType) {
		this.uinType = uinType;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public byte[] getImei() {
		return imei;
	}

	public void setImei(byte[] imei) {
		this.imei = imei;
	}
	
	public byte[] encode(){
		int reqHeadLen = 12;
		if(uin != null){
			reqHeadLen += uin.length;
		}
		reqHeadLen += 24;//Bid + LC + A2Len
		if(A2 != null){
			reqHeadLen += A2.length;			
		}
		reqHeadLen += 4;//ServiceCmdLen
		if(serviceCmd != null){
			reqHeadLen += serviceCmd.length;
		}
		reqHeadLen += 4;//CookieLen
		if(cookie != null){
			reqHeadLen += cookie.length;
		}
		reqHeadLen += 9;//UinType + ErrorCode + IMEILen
		if(imei != null){
			reqHeadLen += imei.length;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(reqHeadLen);
		buf.putInt(reqHeadLen);
		buf.putInt(seqNo);
		if(uin != null){
			buf.putInt(uin.length + 4);			
			buf.put(uin);			
		}
		else{
			buf.putInt(4);						
		}
		buf.putInt(bid);						
		buf.put(lc);						
		
		if(A2 != null){
			buf.putInt(A2.length + 4);						
			buf.put(A2);			
		}
		else{
			buf.putInt(4);									
		}
		
		if(serviceCmd != null){			
			buf.putInt(4 + serviceCmd.length);									
			buf.put(serviceCmd);									
		}
		else{
			buf.putInt(4);												
		}
		
		if(cookie != null){
			buf.putInt(4 + cookie.length);									
			buf.put(cookie);									
			
		}else{
			buf.putInt(4);												
		}
		
		buf.put(uinType);									
		buf.putInt(errorCode);												
		if(imei != null){
			buf.putInt(4 + imei.length);									
			buf.put(imei);												
		}
		else{
			buf.putInt(4);												
		}
		
		return buf.array();
	}
	
	public static ReqHead decode(ByteBuffer buf){
		ReqHead rh = new ReqHead();
		int startPos = buf.position();
		int reqheadLen = buf.getInt();
		rh.setSeqNo(buf.getInt());
		
		int uinLen = buf.getInt();
		if(uinLen > 4){
			byte[] uin = new byte[uinLen - 4];
			buf.get(uin);
			rh.setUin(uin);
		}
		
		rh.setBid(buf.getInt());
		byte[] lc = new byte[16];
		buf.get(lc);
		rh.setLc(lc);

		int a2Len = buf.getInt();
		if(a2Len > 4){
			byte[] A2 = new byte[a2Len - 4];
			buf.get(A2);
			rh.setA2(A2);
		}

		int serviceCmdLen = buf.getInt();
		//System.out.println("serviceCmdLen:" + serviceCmdLen);
		if(serviceCmdLen > 4){
			byte[] serviceCmd = new byte[serviceCmdLen - 4];
			buf.get(serviceCmd);
			rh.setServiceCmd(serviceCmd);
		}

		int cookieLen = buf.getInt();
		if(cookieLen > 4){
			byte[] cookie = new byte[cookieLen - 4];
			buf.get(cookie);
			rh.setCookie(cookie);
		}
		
		rh.setUinType(buf.get());		
		rh.setErrorCode(buf.getInt());
		
		int imeiLen = buf.getInt();
		if(imeiLen > 4){
			byte[] imei = new byte[imeiLen - 4];
			buf.get(imei);
			rh.setImei(imei);
		}
		
		int endPosition = buf.position();
		int realLen = endPosition - startPos;
		//跳过多余的字节 -----应该不会有多余的字节
		if(reqheadLen > realLen){
			buf.position(endPosition + reqheadLen - realLen);
		}
		
		
		return rh;		
	}

}
