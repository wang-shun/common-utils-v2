package com.youzan.sz.jutil.net;

import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.crypto.HexUtil;

/**
 * 详细内容见TCPServer的文档.与TCPServer配套的客户端
 * @author junehuang
 *
 */
public class TCPClient {
    private String ip;
    private int port = 18080;
    private static int MAX_PACK_SIZE = 1024*1024*64;//最大包长64M
    private AtomicInteger seqID = new AtomicInteger(); 
    
    public TCPClient(String ip, int port){
		this.ip = ip;
		this.port = port;
    }
	
	static void readAll(InputStream is,byte[] bs,int start,int end) throws IOException {
		while(start<end){
			int bytesRead = is.read(bs,start,end-start);
			if (bytesRead < 0)
				throw new IOException("close by peer.");
			start += bytesRead;
		}
	}
	
	static ByteBuffer readAll(InputStream is) throws IOException {
		//read packLen
		byte[] bs = new byte[4];
		readAll(is,bs,0,4);
		int packLen = ((bs[0] & 0xff) << 24) |
			      ((bs[1] & 0xff) << 16) | 
			      ((bs[2] & 0xff) << 8) | 
			      ((bs[3] & 0xff) << 0);

		
		//check packLen
		if( packLen<0 || packLen > MAX_PACK_SIZE){
			throw new IOException("PackLen error:"+packLen);
		}
//		System.out.println("packLen = "+packLen);
		
		//read data
		ByteBuffer buf = ByteBuffer.allocate(packLen);
		buf.putInt(packLen);
		readAll(is,buf.array(),4,packLen);
		buf.rewind();//very
		return buf;
	}	

	public byte[] sendAndReceive(byte[] request, int timeout) throws IOException{
    	if( request==null ){
    		request = new byte[0];
    	}
    	
        Socket socket = null;
        InputStream input = null;
        OutputStream output = null;
        try{
            socket = new Socket();
            InetSocketAddress addr = new InetSocketAddress(ip,port);
            socket.connect(addr, timeout);
            socket.setSoTimeout(timeout);
            input = socket.getInputStream();
            output = socket.getOutputStream();
            
            //send
	        int toServerSize = 8+request.length;
			ByteBuffer buf = ByteBuffer.allocate(toServerSize);
			int seq = getNextSeq();
			buf.putInt(toServerSize).putInt(seq).put(request);
			byte[] content = buf.array();
			output.write(content,0,toServerSize);
			output.flush();
            
            //receive
			ByteBuffer recvBuf = readAll(input);

	    	int numBytes = recvBuf.capacity();
			if( numBytes<8 ){
				throw new IOException("pack head err:" + HexUtil.bytes2HexStr(recvBuf.array()));
			}
			//read head,int按照网络顺序读取
			int size = recvBuf.getInt();
			if( size!=numBytes ){
				throw new IOException("pack length err:"+size+"|"+numBytes);
			}
			int seq2 = recvBuf.getInt();
			if( seq!=seq2 ){
				throw new IOException("pack seq err:"+seq+"|"+seq2);
			}
			//read body
			byte[] receiveBytes = new byte[numBytes-8];
			recvBuf.get(receiveBytes);
            return receiveBytes;
        }finally{//关闭连接
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
                if( socket!=null )socket.close();
			} catch (Exception e) {
				//do nothing
			}
        }
    }

    public void sendOnly(byte[] request, int timeout) throws IOException {
        Socket socket = null;
        OutputStream output = null;
        try{
            socket = new Socket();
            InetSocketAddress addr = new InetSocketAddress(ip,port);
            socket.connect(addr, timeout);
            socket.setSoTimeout(timeout);
            output = socket.getOutputStream();
            
            //send
	        int toServerSize = 8+request.length;
			ByteBuffer buf = ByteBuffer.allocate(toServerSize);
			int seq = getNextSeq();
			buf.putInt(toServerSize).putInt(seq).put(request);
			byte[] content = buf.array();
			output.write(content,0,toServerSize);
			output.flush();
        }finally{
        	try {
            	if( output!=null )output.close();
			} catch (Exception e) {
				//do nothing
			}
        	try {
                if( socket!=null )socket.close();
			} catch (Exception e) {
				//do nothing
			}
        }
    }
    private int getNextSeq()
    {
    	int i = seqID.getAndIncrement();
    	return i & Integer.MAX_VALUE;
    }
    
    public static void main(String[] args) throws Exception{
		ByteBuffer buf = ByteBuffer.allocate(5);
		for (int i = 0; i < 5; i++) {
			buf.put((byte)i);
		}
		TCPClient client = new TCPClient("127.0.0.1",1234); 
    	byte[] result = client.sendAndReceive(buf.array(),1000*5);
		System.out.println(Arrays.toString(result));
    	result = client.sendAndReceive(buf.array(),1000*5);
		System.out.println(Arrays.toString(result));
//    	client.sendOnly(buf.array());
	}

}
