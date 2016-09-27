package com.youzan.sz.jutil.net.simple;

import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
//
//import com.qq.jutil.crypto.HexUtil;
//import com.qq.jutil.j4log.Logger;

/**
 * 不public的类，attach在selector中，实现还是有点复杂。
 * @author junehuang
 *
 */
class SessionImpl implements Session{
	SelectionKey   key;
	SocketChannel  channel;
	SessionHandler handler;
	Executor       executor;
	Logger         logger;
	SessionStatus  status;
	Selector       selector;

	//读缓冲
	private ByteBuffer smallBuf = ByteBuffer.allocate( 1024*16 );//常驻内存的读缓冲区16K
	private ByteBuffer bigBuf;//动态分配缓冲区，处理包长大于smallBuf的请求，请求收取下来以后该缓冲区就立即释放
	private ByteBuffer buf = smallBuf;//当前缓冲区
	//写缓冲
	private Queue<ByteBuffer> queue = new ArrayBlockingQueue<ByteBuffer>(1024*8);//常驻内存的写缓冲区8K,如果每个bufer 1k的话，最大就是8M
	
	private static final int MAX_PACK_SIZE = 1024*1024*64;//最大包长64M
	
    private static final Logger debugLog = Logger.getLogger("jutil");

    SessionImpl( Selector selector, SessionHandler handler, Executor executor){
    	this.selector = selector;
		this.handler = handler;
		this.executor = executor;
	}
	
	public void close(){
		status = SessionStatus.CLOSED;
		try {
			if( channel!=null )
				channel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if( key!=null )
				key.cancel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//清空发送接收缓存，避免此session被其他对象引用而导致的内存泄漏
		try {
			queue = null;
			smallBuf = null;
			bigBuf = null;
			buf = null;
			key = null;
			channel = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int doRead() throws IOException{
		int readCount = 0;
		int cnt = channel.read( buf );
		if( cnt==0 )
			throw new IOException("I cann't believe it happened...");
		if( cnt>0 ){
			ByteBuffer bufView = buf.duplicate();
			bufView.flip();
			//循环读一个请求
			while( true ){
				Packet pack = getOnePacket(bufView);
				if( pack != null ){
					readCount++;
					try {
						executor.execute( new WorkThread( pack ) );
					} catch (Throwable e) {//可能线程池满，这个时候不要断开网络连接
						e.printStackTrace();
						debugLog.error( "SimpleSession threadPool.execute error: " + e.getMessage());
					}
				}else{
					break;
				}
			}
			//bufView分3中情况，读完了,部分没读完,根本就没有读
			if( bufView.position()==bufView.limit() ){//读完了
				bigBuf = null;
				buf = smallBuf;
				buf.clear();
			}else if( bufView.position() == 0 ){//根本就没有读
				//do nothing
			}else{//部分没读完
				buf.clear();
				for (int i = bufView.position(); i < bufView.limit(); i++) {
					buf.put( bufView.get( i ) );
				}
			}
//				System.out.println("buf status: "+buf.position()+","+buf.limit()+","+buf.capacity()+","+(bigBuf==null?0:bigBuf.capacity()));
		}else{//客户端主动关闭连接
			readCount = -1;
			this.close();
		}
		return readCount;
	}
	
	public int doWrite() throws IOException{
		int writeCount = 0;
		while( true ){
			ByteBuffer wBuf = queue.peek();
			if( wBuf==null ){//仅仅只关注读
				key.interestOps( SelectionKey.OP_READ );
				if( queue.peek()!=null )//防止write()操作导致的多线程问题
					key.interestOps( SelectionKey.OP_READ | SelectionKey.OP_WRITE );
				break;
			}
			channel.write( wBuf );
			if( wBuf.remaining() == 0 ){//写完了一个包
				writeCount++;
				queue.remove();
				continue;
			}else{
				break;
			}
		}
		return writeCount;
	}
	
	
	public void write( int seq, byte[] bs) throws IOException{
		if( status == Session.SessionStatus.CLOSED )
			throw new IOException( "SimpleSession is closed" );
		int toServerSize = 8+bs.length;
		if( toServerSize > MAX_PACK_SIZE )
			throw new IOException("SimpleSession write error: package size greater than "+MAX_PACK_SIZE);
		ByteBuffer wBuf = ByteBuffer.allocate(toServerSize);
		wBuf.putInt(toServerSize).putInt(seq).put(bs);
		wBuf.flip();
		if( !queue.offer(wBuf) )
			throw new IOException("SimpleSession write queue if full: "+queue.size());
		if( key!=null )
			key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
		selector.wakeup();
	}
	
	public SessionStatus getState(){
		return status;
	}
	
	private Packet getOnePacket(ByteBuffer bufView) throws IOException{
		if( bufView.position() > bufView.limit()-4 ){
			return null;
		}
		int packLen = bufView.getInt(bufView.position());
		if( packLen<8 || packLen > MAX_PACK_SIZE )
			throw new IOException("PackLen error:"+packLen);
		if( packLen > smallBuf.capacity() && buf==smallBuf ){
			bigBuf = ByteBuffer.allocate( packLen );
			buf = bigBuf;
			if( bufView.position()==0 ){
				for (int i = bufView.position(); i < bufView.limit(); i++) {
					buf.put( bufView.get( i ) );
				}
			}
			return null;
		}
		if( bufView.limit() < bufView.position() + packLen ){//一个包没有读完整
			return null;
		}
		
		//读出一个完整的数据包
		int size = bufView.getInt();//packLen
		int seq = bufView.getInt();
		byte[] bs = new byte[packLen-8];
		bufView.get( bs );
		Packet request = new PacketImpl(
				(InetSocketAddress)channel.socket().getRemoteSocketAddress(),
				size,
				seq,
				bs);
		
		return request;
	}
	
    public void setLogger(String logName){
		if( logName!=null && logName.length()>0 ){
			this.logger = Logger.getLogger(logName);
		}else{
			this.logger = null;
		}
    }
	private class WorkThread implements Runnable {
		Packet packet;
		WorkThread(Packet packet){
			this.packet = packet;
		}
		public void run(){
        	long start = System.currentTimeMillis();
        	byte[] front = {};
        	int len=0;
			try {
				len = packet.getBody().length;
				handler.packetReceived(SessionImpl.this, packet);
				if( logger!=null ){
		        	front = getFrontBytes(packet.getBody(),10);
					logger.info(packet.getAddress().getAddress().getHostAddress() + "|succ|" + (System.currentTimeMillis()-start) + "|" + HexUtil
							.bytes2HexStr(front) + "|" + len);
				}
			} catch (Throwable e) {//非网络异常不要断开连接
				e.printStackTrace();
				if( logger!=null ){
		        	front = getFrontBytes(packet.getBody(),10);
					logger.error(packet.getAddress().getAddress().getHostAddress()+"|"+e.getClass().getName()+":"+e.getMessage()+"|"+(System.currentTimeMillis()-start)+"|"+HexUtil.bytes2HexStr(front)+"|"+len);
				}
			}
		}
	}
	private static byte[] getFrontBytes(byte[] receiveBytes, int maxLen){
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
}
