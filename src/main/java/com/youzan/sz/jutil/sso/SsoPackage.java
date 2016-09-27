package com.youzan.sz.jutil.sso;

import com.youzan.sz.jutil.crypto.HexUtil;

import java.nio.ByteBuffer;
import java.util.Arrays;

//import com.qq.jutil.crypto.HexUtil;

/**
 * 
 * SSO->业务 && 业务->SSO的编码和解码 
 * 《无线终端统一接入协议(v2.0).doc》
 * 
 * 编码：
    private static byte[] makeSSO(int seq, String serviceCmd, byte[] srcData) {
		SsoPackage sp = new SsoPackage();
		SsoHead sh = new SsoHead();
		sp.setSsoHead(sh);
		
		ReqHead rh = new ReqHead();
		rh.setSeqNo(seq);
		rh.setServiceCmd(serviceCmd.getBytes());
		sp.setReqHead(rh);
		
		sp.setBusiBuf(srcData);
		
		return sp.encode();
	}
		
 *解码：
 *     private static byte[] decodeSSO(byte[] buffer){
    	SsoPackage sp = SsoPackage.decode(buffer);
		return sp.getBusiBuf();
    }

	注意：busiBuf如果是自定义的协议，为了和wup兼容，前4个自己需要是整个busiBuf包的长度
 * 
 * @author markzhang
 *
 */
public class SsoPackage {
	private SsoHead ssoHead;
	private ReqHead reqHead;
	private byte[] busiBuf;
	
	public SsoHead getSsoHead() {
		return ssoHead;
	}
	public void setSsoHead(SsoHead ssoHead) {
		this.ssoHead = ssoHead;
	}
	public ReqHead getReqHead() {
		return reqHead;
	}
	public void setReqHead(ReqHead reqHead) {
		this.reqHead = reqHead;
	}
	public byte[] getBusiBuf() {
		return busiBuf;
	}
	public void setBusiBuf(byte[] busiBuf) {
		this.busiBuf = busiBuf;
	}
	
	public byte[] encode(){
		byte[] ssoHeadBytes = ssoHead.encode();
		byte[] reqHeadBytes = reqHead.encode();
		int len = 4 + ssoHeadBytes.length + reqHeadBytes.length;
		if(busiBuf != null){
			len += busiBuf.length;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(len);
		buf.putInt(len);
		buf.put(ssoHeadBytes);
		buf.put(reqHeadBytes);
		if(busiBuf != null){
			buf.put(busiBuf);
		}
		else{
			buf.putInt(4);			
		}
		
		return buf.array();
	}

	public static SsoPackage decode(byte[] bytes){
		SsoPackage sp = new SsoPackage();
		
    	ByteBuffer buf = ByteBuffer.wrap(bytes);
    	int totalLen = buf.getInt();
    	
		sp = decode(totalLen, buf);
		return sp;
	}
	


	/**
	 * 解sso包
	 * 
	 * @param totalLen
	 * @param buf 读取过totalLen，或者没有totalLen的数据
	 * @param heartbeat，是否是心跳包，心跳包SsoHead部分少4个字段
	 * @return
	 */
	public static SsoPackage decode(int totalLen, ByteBuffer buf) {
		SsoPackage sp = new SsoPackage();
		
    	SsoHead sh = SsoHead.decode(buf);
    	sp.setSsoHead(sh);
    	
    	ReqHead rh = ReqHead.decode(buf);
    	sp.setReqHead(rh);
    	
    	int busiLen = buf.getInt();
    	if(busiLen > 4){
    		byte[] busi = new byte[busiLen];
    		buf.position(buf.position() - 4);
    		buf.get(busi);
    		sp.setBusiBuf(busi);
    	}
		
		return sp;
	}
	

	
	
	public static void main(String[] args){
		String input =         "000000FF0000001800000000" +
				       "00000000000000000000000000000000" +
				       "00000036000000010000000400000000" +
				       "00000000000000000000000000000000" +
				       "00000004000000050000000004000000" +
				       "000000000004000000AD10022C3C400C560773657276616E74660866756E6374696F6E7D0001008608000106137273705F6164645F73686F70666565646D73671800010623517A4C6966654D6F62696C652E537663526573706F6E736541646453686F70466565641D0000420A0A01FED4162EE682A8E5B7B2E8AF84E8AEBAE8BF87E8AFA5E9A490E58E85EFBC8CE4B88DE58FAFE9878DE5A48DE782B9E8AF84212002324C93500346000B16000B8C980CA80C";
		
		
		byte[] bytes = HexUtil.hexStr2Bytes(input);
		SsoPackage pack = decode(bytes);
		System.out.println(Arrays.toString(pack.getBusiBuf()));
		
		String input2 =  "0000001800000000" +
	       "00000000000000000000000000000000" +
	       "00000036000000010000000400000000" +
	       "00000000000000000000000000000000" +
	       "00000004000000050000000004000000" +
	       "000000000004000000AD10022C3C400C560773657276616E74660866756E6374696F6E7D0001008608000106137273705F6164645F73686F70666565646D73671800010623517A4C6966654D6F62696C652E537663526573706F6E736541646453686F70466565641D0000420A0A01FED4162EE682A8E5B7B2E8AF84E8AEBAE8BF87E8AFA5E9A490E58E85EFBC8CE4B88DE58FAFE9878DE5A48DE782B9E8AF84212002324C93500346000B16000B8C980CA80C";

		byte[] bytes2 = HexUtil.hexStr2Bytes(input2);
		ByteBuffer buf = ByteBuffer.wrap(bytes2);
		SsoPackage pack2 = decode(255,buf);
		
		System.out.println(Arrays.toString(pack2.getBusiBuf()));
	}
	
	
}
