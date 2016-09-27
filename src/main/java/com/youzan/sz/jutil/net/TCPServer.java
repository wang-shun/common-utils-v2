package com.youzan.sz.jutil.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.j4log.Logger;


/**
 * TCP短连接服务,网络和应用分离后的网络部分封装
 * @author junehuang
 * 
 * TCPServer能够做到网络和业务完全分离,开发人员只需要关注业务方面.也就是开发人员只需要实现AppProcessor接口即可
 * <br/>
 * 一个简单的Echo服务的例子：<br/>
 * 服务器代码：<br/>
 *import com.qq.jutil.net.TCPServer;<br/>                                                                
 *import com.qq.jutil.net.AppProcessor;<br/>
 *import java.net.InetAddress;<br/>
 *public class EchoServer{<br/>
 *&nbsp;public static void main(String[] args) {<br/>
 *&nbsp;&nbsp;		AppProcessor processor = new AppProcessor(){<br/>
 *&nbsp;&nbsp;&nbsp;			public byte[] process(RequestObj req){<br/>
 *&nbsp;&nbsp;&nbsp;&nbsp;		    return req.getData();<br/>
 *&nbsp;&nbsp;&nbsp;			}<br/>
 *&nbsp;&nbsp;		};<br/>
 *&nbsp;&nbsp;		new TCPServer("127.0.0.1",1234,processor).start();<br/>
 *&nbsp;	}<br/>
 *}<br/>
 *<br/>                                                                                           
 *
 * 客户端代码：<br/>
 *import com.qq.jutil.net.TCPClient;<br/>                                                                
 *import java.nio.ByteBuffer;;<br/>                                                                
 *import java.util.Arrays;;<br/>
 *import java.net.InetAddress;<br/>
 *public class EchoClient{<br/>
 *&nbsp;public static void main(String[] args) {<br/>
 *&nbsp;&nbsp;		ByteBuffer buf = ByteBuffer.allocate(100);<br/>
 *&nbsp;&nbsp;		for (int i = 0; i < 100; i++) {<br/>
 *&nbsp;&nbsp;&nbsp;		buf.put((byte)i);<br/>
 *&nbsp;&nbsp;		}<br/>
 *&nbsp;&nbsp;		TCPClient client = new TCPClient("127.0.0.1",1234);<br/>
 *&nbsp;&nbsp;		byte[] result = client.sendAndReceive(buf.array(),1000*5);<br/>
 *&nbsp;&nbsp;		System.out.println(Arrays.toString(result));<br/>
 *&nbsp;	}<br/>
 *}<br/>
 */
public class TCPServer extends Thread{
	//必须的，构造方法必须指定
    private String ip;
    private int port = 18080;
    private AppProcessor processor;
    
    //可以通过TCPServer.setXXX()设置的属性
    private int minThreadNum=5;
    private int maxThreadNum=100;
    private int readTimeout=1000*15;

    private ServerSocket ss;
    private ThreadPoolExecutor threadPool;
    private static final Logger logger = Logger.getLogger("TCPAccessLog");
    private static final Logger debugLog = Logger.getLogger("jutil");
    
    private class WorkThread implements Runnable {
    	private Socket sock;
    	public WorkThread(Socket sock){
    		this.sock = sock;
    	}
    	public void run(){
        	long start = System.currentTimeMillis();
    		InetAddress ip = null;
    		int port = 0;
    		int numBytes = 0;//tcp包的实际大小
    		byte[] receiveBytes = {};//接收的业务数据，不包括包头
    		byte[] front = {};//仅仅为了打日志
            InputStream input = null;
            OutputStream output = null;
    		try {
        		ip = sock.getInetAddress();
        		port = sock.getPort();
                sock.setSoTimeout(readTimeout);
                input = sock.getInputStream();
                output = sock.getOutputStream();
    			ByteBuffer recvBuf = TCPClient.readAll(input);
        		
        		
    	    	numBytes = recvBuf.capacity();
    			if( numBytes<8 ){
    				System.err.println("pack head err:"+ip.getHostAddress()+"|"+HexUtil.bytes2HexStr(recvBuf.array()));
    				return;
    			}
    			//read head,int按照网络顺序读取
    			byte[] head = new byte[8];
    			int size = recvBuf.getInt();
    			if( size!=numBytes ){
    				System.err.println("pack length err:"+ip.getHostAddress()+"|"+size+"|"+numBytes);
    				return;
    			}
    			int seq = recvBuf.getInt();
    			//read body
    			receiveBytes = new byte[numBytes-8];
    			recvBuf.get(receiveBytes);
    			front = getFrontBytes(receiveBytes,10);
//				System.out.println("size:"+size+",seq:"+seq+","+HexUtil.bytes2HexStr(receiveBytes));
    			
    			//process
    			byte[] result = processor.process(new TCPRequest(ip,port,receiveBytes,seq));
    	        if(result == null){//don't send resp back
        			logger.info(ip.getHostAddress()+"|succ|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
        			return;
    	        }

    			//response
    	        int toClientSize = 8+result.length;
    			ByteBuffer buf = ByteBuffer.allocate(toClientSize);
    			buf.putInt(toClientSize).putInt(seq).put(result);
    			byte[] content = buf.array();
    			output.write(content,0,toClientSize);
    			output.flush();
    			logger.info(ip.getHostAddress()+"|succ|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
    		} catch (Exception e) {
    			logger.error(ip.getHostAddress()+"|"+e.getMessage()+"|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front));
//    			e.printStackTrace();
    		} finally {
            	try {
                	if( input!=null )input.close();
    			} catch (Exception e) {
    				//do nothing
    			}
            	try {
                	if( output!=null )output.close();
    			} catch (Exception e) {
    				//do nothing
    			}
    			try {
					if( sock!=null )sock.close();
				} catch (Exception e) {
					//donothing
				}
    		}
    	}
    }
    public TCPServer(int port, AppProcessor processor){
    	this(null,port,processor);
    }
    public TCPServer(String ip, int port, AppProcessor processor){
		this.ip = ip;
		this.port = port;
		this.processor = processor;
	}
	public void run(){
		try {
			//listen port
			if( ip==null ){
				ss = new ServerSocket(port);
			}else{
				ss = new ServerSocket(port, 50, InetAddress.getByName(ip));
			}
			
			//init thread pool
			if( maxThreadNum<minThreadNum ){
				maxThreadNum = minThreadNum;
			}
			threadPool = new ThreadPoolExecutor(minThreadNum, maxThreadNum, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
			System.out.println("TCPServer is running......");
			
			while (true)
			{
				Socket socket = null;
				try
				{
					socket = ss.accept();
					threadPool.execute(new WorkThread(socket));
				}
				catch (Exception e)
				{
					debugLog.error("", e);
 				}
			}
		} catch (Exception e) {
			debugLog.error("", e);
		} finally {
			try {
				if( ss!=null )ss.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
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
	
	private static class TCPRequest implements RequestObj{
		InetAddress srcAddr;
		int port;
		byte[] data;
		int seq;
		private TCPRequest(InetAddress srcAddr, int port, byte[] data,int seq){
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
	
	public int getReadTimeout() {
		return readTimeout;
	}
	
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
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
		new TCPServer("127.0.0.1",1234,processor).start();
	}
}
