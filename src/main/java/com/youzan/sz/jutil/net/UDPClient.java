package com.youzan.sz.jutil.net;

import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.crypto.HexUtil;
//import com.qq.tac2.jdt.share.AsyncClientInfoBox;

/**
 * 详细内容见UDPServer的文档.与UDPServer配套的客户端
 * @author junehuang
 *
 */
public class UDPClient {
    private String ip;
    private int port = 18080;
    private int bufferSize = 8192;
    private AtomicInteger seqID = new AtomicInteger(); 
    
    public UDPClient(String ip, int port){
		this.ip = ip;
		this.port = port;
    }
	
    public byte[] sendAndReceive(byte[] request, int timeout) throws IOException{
    	if( request==null ){
    		request = new byte[0];
    	}
    	
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket(0);
            ds.connect(InetAddress.getByName(ip),port);
            ds.setSoTimeout(timeout);
            
            //send
	        int toServerSize = 8+request.length;
			ByteBuffer buf = ByteBuffer.allocate(toServerSize);
			int seq = getNextSeq();
			buf.putInt(toServerSize).putInt(seq).put(request);
            DatagramPacket outgoing = new DatagramPacket(buf.array(), buf.capacity(), ds.getInetAddress(), ds.getPort());
            ds.send(outgoing);
            
            //receive
            byte[] buffer = new byte[bufferSize];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            ds.receive(dp);

	    	int numBytes = dp.getLength();
			if( numBytes<8 ){
    			byte[] head = new byte[numBytes];
    			System.arraycopy(dp.getData(), 0, head, 0, numBytes);
				throw new IOException("pack head err:" + HexUtil.bytes2HexStr(head));
			}
			//read head,int按照网络顺序读取
			byte[] head = new byte[8];
			System.arraycopy(dp.getData(), 0, head, 0, 8);
			int size = ((head[0] & 0xff) << 24
					|(head[1] & 0xff) << 16
					|(head[2] & 0xff) << 8
					|(head[3] & 0xff) << 0);
			if( size!=numBytes ){
				throw new IOException("pack length err:"+size+"|"+numBytes);
			}
			int seq2 = ((head[4] & 0xff) << 24
					|(head[5] & 0xff) << 16
					|(head[6] & 0xff) << 8
					|(head[7] & 0xff) << 0);
			if( seq!=seq2 ){
				throw new IOException("pack seq err:"+seq+"|"+seq2);
			}
			//read body
			byte[] receiveBytes = new byte[numBytes-8];
			System.arraycopy(dp.getData(), 8, receiveBytes, 0, numBytes-8);
            return receiveBytes;
        }finally{
            if( ds!=null )ds.close();
        }
    }

    public void sendOnly(byte[] request) throws IOException {
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket(0);
            ds.connect(InetAddress.getByName(ip),port);
            
            //send
	        int toServerSize = 8+request.length;
			ByteBuffer buf = ByteBuffer.allocate(toServerSize);
			int seq = getNextSeq();
			buf.putInt(toServerSize).putInt(seq).put(request);
            DatagramPacket outgoing = new DatagramPacket(buf.array(), buf.capacity(), ds.getInetAddress(), ds.getPort());
            ds.send(outgoing);
        }finally{
            if(ds!=null)ds.close();
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
		UDPClient client = new UDPClient("127.0.0.1",1234); 
    	byte[] result = client.sendAndReceive(buf.array(),1000*5);
		System.out.println(Arrays.toString(result));
    	result = client.sendAndReceive(buf.array(),1000*5);
		System.out.println(Arrays.toString(result));
//    	client.sendOnly(buf.array());
	}
    
    /**
	 * 异步发送 不阻塞 等待box唤醒回调
	 * @param box
	 * @param bs
	 * @param timeout
	 * @return
	 * @throws IOException
	 */
//	public boolean  tacAsync_sendAndReceive( AsyncClientInfoBox box , byte[] bs, int timeout ) throws IOException{
//		throw new RuntimeException("not supprot asyncUdp, TODO");
//	}
//
//	public boolean  tacAsync_sendAndReceive_tac2(AsyncClientInfoBox __box__) throws IOException {
//		throw new RuntimeException("not supprot asyncUdp, TODO");
//	}

}
