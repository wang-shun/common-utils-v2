package com.youzan.sz.jutil.net;

import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.qq.jutil.crypto.HexUtil;
//import com.qq.jutil.j4log.Logger;


/**
 * UDP服务,网络和应用分离后的网络部分封装
 * @author junehuang
 * 
 * UDPServer能够做到网络和业务完全分离,开发人员只需要关注业务方面.也就是开发人员只需要实现AppProcessor接口即可
 * <br/>
 * 一个简单的Echo服务的例子：<br/>
 * 服务器代码：<br/>
 *import com.qq.jutil.net.UDPServer;<br/>                                                                
 *import com.qq.jutil.net.AppProcessor;<br/>
 *import java.net.InetAddress;<br/>
 *public class EchoServer{<br/>
 *&nbsp;public static void main(String[] args) {<br/>
 *&nbsp;&nbsp;		AppProcessor processor = new AppProcessor(){<br/>
 *&nbsp;&nbsp;&nbsp;			public byte[] process(RequestObj req){<br/>
 *&nbsp;&nbsp;&nbsp;&nbsp;		    return req.getData();<br/>
 *&nbsp;&nbsp;&nbsp;			}<br/>
 *&nbsp;&nbsp;		};<br/>
 *&nbsp;&nbsp;		new UDPServer("127.0.0.1",1234,processor).start();<br/>
 *&nbsp;	}<br/>
 *}<br/>
 *<br/>                                                                                           
 *
 * 客户端代码：<br/>
 *import com.qq.jutil.net.UDPClient;<br/>                                                                
 *import java.nio.ByteBuffer;;<br/>                                                                
 *import java.util.Arrays;;<br/>
 *import java.net.InetAddress;<br/>
 *public class EchoClient{<br/>
 *&nbsp;public static void main(String[] args) {<br/>
 *&nbsp;&nbsp;		ByteBuffer buf = ByteBuffer.allocate(100);<br/>
 *&nbsp;&nbsp;		for (int i = 0; i < 100; i++) {<br/>
 *&nbsp;&nbsp;&nbsp;		buf.put((byte)i);<br/>
 *&nbsp;&nbsp;		}<br/>
 *&nbsp;&nbsp;		UDPClient client = new UDPClient("127.0.0.1",1234);<br/>
 *&nbsp;&nbsp;		byte[] result = client.sendAndReceive(buf.array(),1000*5);<br/>
 *&nbsp;&nbsp;		System.out.println(Arrays.toString(result));<br/>
 *&nbsp;	}<br/>
 *}<br/>
 */
public class UDPServer extends Thread{
	//必须的，构造方法必须指定
    private String ip;
    private int port = 18080;
    private AppProcessor processor;
    
    //可以通过UDPServer.setXXX()设置的属性
    private int bufferSize = 8192;
    private int minThreadNum=5;
    private int maxThreadNum=100;

    private DatagramSocket ds;
    private ThreadPoolExecutor threadPool;
    private static final Logger logger   = Logger.getLogger("UDPAccessLog");
    private static final Logger debugLog = Logger.getLogger("jutil");
    
    private class WorkThread implements Runnable {
    	private DatagramPacket dp;
    	public WorkThread(DatagramPacket dp){
    		this.dp = dp;
    	}
    	public void run(){
        	long start = System.currentTimeMillis();
    		InetAddress ip = null;
    		int port = 0;
    		int numBytes = 0;//udp包的实际大小
    		byte[] receiveBytes = {};//接收的业务数据，不包括包头
    		byte[] front = {};//仅仅为了打日志
    		try {
        		ip = dp.getAddress();
        		port = dp.getPort();
    	    	numBytes = dp.getLength();
    			if( numBytes<8 ){
        			byte[] head = new byte[numBytes];
        			System.arraycopy(dp.getData(), 0, head, 0, numBytes);
    				System.err.println("pack head err:" + ip.getHostAddress() + "|" + HexUtil.bytes2HexStr(head));
    				return;
    			}
    			//read head,int按照网络顺序读取
    			byte[] head = new byte[8];
    			System.arraycopy(dp.getData(), 0, head, 0, 8);
    			int size = ((head[0] & 0xff) << 24
    					|(head[1] & 0xff) << 16
    					|(head[2] & 0xff) << 8
    					|(head[3] & 0xff) << 0);
    			if( size!=numBytes ){
    				System.err.println("pack length err:"+ip.getHostAddress()+"|"+size+"|"+numBytes);
    				return;
    			}
    			int seq = ((head[4] & 0xff) << 24
    					|(head[5] & 0xff) << 16
    					|(head[6] & 0xff) << 8
    					|(head[7] & 0xff) << 0);
    			//read body
    			receiveBytes = new byte[numBytes-8];
    			System.arraycopy(dp.getData(), 8, receiveBytes, 0, numBytes-8);
    			front = getFrontBytes(receiveBytes,10);
//				System.out.println("size:"+size+",seq:"+seq+","+HexUtil.bytes2HexStr(receiveBytes));
    			
    			//process
    			byte[] result = processor.process(new UDPRequest(ip,port,receiveBytes,seq));
    	        if(result == null){//don't send resp back
        			logger.info(ip.getHostAddress()+"|succ|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
        			return;
    	        }

    			//response
    	        int toClientSize = 8+result.length;
    			ByteBuffer buf = ByteBuffer.allocate(toClientSize);
    			buf.putInt(toClientSize).putInt(seq).put(result);
    			DatagramPacket outgoing = new DatagramPacket(buf.array(), buf.capacity(), dp.getAddress(), dp.getPort());
    			ds.send(outgoing);
    			logger.info(ip.getHostAddress()+"|succ|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
    		} catch (Exception e) {
    			logger.error(ip.getHostAddress()+"|"+e.getMessage()+"|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
//    			e.printStackTrace();
    		}
    	}
    }
    public UDPServer(int port, AppProcessor processor){
    	this(null,port,processor);
    }
    public UDPServer(String ip, int port, AppProcessor processor){
		this.ip = ip;
		this.port = port;
		this.processor = processor;
	}
	public void run(){
		try {
			//listen port
			if( ip==null ){
				ds = new DatagramSocket(port);
			}else{
				ds = new DatagramSocket(port, InetAddress.getByName(ip));
			}
			
			//init thread pool
			if( maxThreadNum<minThreadNum ){
				maxThreadNum = minThreadNum;
			}
			threadPool = new ThreadPoolExecutor(minThreadNum, maxThreadNum, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
			System.out.println("UDPServer is running......");
			
			while(true){
				byte[] buffer = new byte[bufferSize];
				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
				try{
					//accept
					ds.receive(incoming);  //接收请求
					//new a work thread
					//run work thread by thread pool
					threadPool.execute(new WorkThread(incoming));
				}catch(Exception e){
					debugLog.error("", e);
				}
				
			}
		} catch (Exception e) {
			debugLog.error("", e);
		}
	}
	
	private byte[] getFrontBytes(byte[] receiveBytes, int maxLen){
		if(receiveBytes==null || receiveBytes.length==0)
			return new byte[0];
		
		byte[] frontBytes = {};
		int len = receiveBytes.length; 
		if(len>maxLen){
			frontBytes = new byte[maxLen];
			System.arraycopy(receiveBytes, 0, frontBytes, 0, maxLen);
		}else{
			frontBytes = new byte[len];
			System.arraycopy(receiveBytes, 0, frontBytes, 0, len);
		}
		return frontBytes;
	}
	
	private static class UDPRequest implements RequestObj{
		InetAddress srcAddr;
		int port;
		byte[] data;
		int seq;
		private UDPRequest(InetAddress srcAddr, int port, byte[] data,int seq){
			this.srcAddr = srcAddr;
			this.port = port;
			this.data = data;
			this.seq = seq;
		}
		public InetAddress getAddress() {
			return this.srcAddr;
		}
		public byte[] getData() {
			return this.data;
		}
		public int getPort() {
			return this.port;
		}
		public int getSeq() {
			return this.seq;
		}
		
	}
	
	
	/**
	 * 从Server主动发送
	 * @param seq
	 * @param addr
	 * @param port
	 * @param data
	 * @throws IOException
	 */
	public void send(int seq,InetAddress addr,int port, byte[] data) throws IOException{
        int toClientSize = 8+data.length;
		ByteBuffer buf = ByteBuffer.allocate(toClientSize);
		buf.putInt(toClientSize).putInt(seq).put(data);
		DatagramPacket outgoing = new DatagramPacket(buf.array(), buf.capacity(), addr, port);
		ds.send(outgoing);
	}
	

	
	//flower is Getter and Setters.............................
	public int getMaxThreadNum() {
		return maxThreadNum;
	}
	public void setMaxThreadNum(int maxThreadNum) {
		this.maxThreadNum = maxThreadNum;
	}
	
	
	public int getMinThreadNum() {
		return minThreadNum;
	}
	public void setMinThreadNum(int minThreadNum) {
		this.minThreadNum = minThreadNum;
	}
	
	public AppProcessor getProcessor()
	{
		return this.processor;
	}
	
	public ThreadPoolExecutor getThreadPool(){
		return threadPool;
	}
	public static void main(String[] args) {
		AppProcessor processor = new AppProcessor(){
			public byte[] process(RequestObj req){
				return req.getData();
			}
		};
		new UDPServer("127.0.0.1",1234,processor).start();
	}
}
