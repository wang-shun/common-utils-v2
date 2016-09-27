package com.youzan.sz.jutil.net.cap;

import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

//import com.qq.jutil.crypto.HexUtil;
//import com.qq.jutil.j4log.Logger;

/**
 * 不public的类，attach在selector中，实现还是有点复杂。
 * @author junehuang
 *
 */
class CapSessionImpl implements CapSession{
	SelectionKey      key;
	SocketChannel     channel;
	CapSessionHandler handler;
	Executor          executor;
	Logger            logger;
	CapSessionStatus  status;

	final int SIZE_OFFSET;
	final int SIZE_LEN;
	final int SEQ_OFFSET;
	final int SEQ_LEN;
	final int MIN_PACK_SIZE;
	
	private String remoteSocketDesc = null;

	// 读缓冲
	private ByteBuffer smallBuf = ByteBuffer.allocate(1024 * 16);// 常驻内存的读缓冲区16K
	private ByteBuffer bigBuf;// 动态分配缓冲区，处理包长大于smallBuf的请求，请求收取下来以后该缓冲区就立即释放
	private ByteBuffer buf = smallBuf;// 当前缓冲区
	// 写缓冲
	private Queue<ByteBuffer> queue = new LinkedBlockingQueue<ByteBuffer>(1024 * 8);// 常驻内存的写缓冲区8K,如果每个bufer
																					// 1k的话，最大就是8M

	private static final int MAX_PACK_SIZE = 1024 * 1024 * 10;// 最大包长10M

	private static final Logger debugLog = Logger.getLogger("jutil");

	// 用于线程池满的时候框架回调业务的接口，默认为一个空实现
	private CapRejectedExecutionHandler rejectedExecutionHandler = null;

	CapSessionImpl(CapSessionHandler handler, Executor executor, CapDef capDef, CapRejectedExecutionHandler rejectedExecutionHandler, Map map) {
		this.handler = handler;
		this.executor = executor;
		this.rejectedExecutionHandler = rejectedExecutionHandler;
		SIZE_OFFSET = capDef.SIZE_OFFSET;
		SIZE_LEN = capDef.SIZE_LEN;
		SEQ_OFFSET = capDef.SEQ_OFFSET;
		SEQ_LEN = capDef.SEQ_LEN;
		MIN_PACK_SIZE = Math.max(SIZE_OFFSET + SIZE_LEN, SEQ_OFFSET + SEQ_LEN);
	}

	public void close(){
		status = CapSessionStatus.CLOSED;
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
			while (true) {
				CapPacket pack = getOnePacket(bufView);
				if (pack != null) {
					readCount++;
					try {
						executor.execute(new WorkThread(pack));
					} catch (RejectedExecutionException e) {
						e.printStackTrace();
						debugLog.error("CapSession threadPool.execute error: " + e.getMessage());
						// 线程池满，尝试调用业务定制的CapRejectedExecutionHandler，默认的是一个空实现，不做任何事情
						rejectedExecutionHandler.handle(this, pack);
					}
				} else {
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
				if( buf==smallBuf ){
					buf.clear();
					for (int i = bufView.position(); i < bufView.limit(); i++) {
						buf.put( bufView.get( i ) );
					}
				}
			}
//				System.out.println("buf status: pos="+buf.position()+",limit="+buf.limit()+",cap="+buf.capacity()+",bigcap="+(bigBuf==null?0:bigBuf.capacity()));
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
	
	
	public void write( int seq, byte[] packet ) throws IOException{
		this.write(seq, packet,0,packet.length);
	}
	
	public void write( int seq, byte[] packet, int pos, int len ) throws IOException{
		//发送前简单检查
		int size = len;
		if( status == CapSession.CapSessionStatus.CLOSED )
			throw new IOException( "CapSession is closed" );
		if( size > MAX_PACK_SIZE )
			throw new IOException("CapSession write error: package size greater than "+MAX_PACK_SIZE);
		if( size<this.MIN_PACK_SIZE )
			throw new IOException("CapSession write error: package size less than "+this.MIN_PACK_SIZE);
		ByteBuffer wBuf = ByteBuffer.wrap( packet,pos,len );
		if( this.SIZE_LEN==2 ){
			if( size>Character.MAX_VALUE )
				throw new IOException("CapSession gnpWrite error: package size greater than Character.MAX_VALUE");
			wBuf.putShort( this.SIZE_OFFSET, (short)size );
		}
		
		//写入size
		if( this.SIZE_LEN==4 )
			wBuf.putInt( this.SIZE_OFFSET, size );

		//写入seq
		if( this.SEQ_LEN==2 )
			wBuf.putShort( this.SEQ_OFFSET, (short)seq );
		else if( this.SEQ_LEN==4 )
			wBuf.putInt( this.SEQ_OFFSET, seq );
		
		if( !queue.offer(wBuf) )
			throw new IOException("CapSession write queue if full: "+queue.size());
		if( key!=null )
			key.interestOps( key.interestOps() | SelectionKey.OP_WRITE );
		key.selector().wakeup();
	}

	public CapSessionStatus getState(){
		return status;
	}
	
	private CapPacket getOnePacket(ByteBuffer bufView) throws IOException{
		//保证至少已经接收到了size字段
		if( bufView.position() > bufView.limit()-this.SIZE_OFFSET-this.SIZE_LEN ){
			return null;
		}
		int packLen = bufView.getChar( bufView.position()+this.SIZE_OFFSET );
		if( this.SIZE_LEN==4 )
			packLen = bufView.getInt( bufView.position()+this.SIZE_OFFSET );
		if( packLen<this.MIN_PACK_SIZE || packLen > MAX_PACK_SIZE )
			throw new IOException("PackLen error:"+packLen);
		if( packLen > smallBuf.capacity() && buf==smallBuf ){
			bigBuf = ByteBuffer.allocate( packLen );
			buf = bigBuf;
			for (int i = bufView.position(); i < bufView.limit(); i++) {
				buf.put( bufView.get( i ) );
			}
			return null;
		}
		if( bufView.limit() < bufView.position() + packLen ){//一个包没有读完整
			return null;
		}
		
		//读出一个完整的数据包
		int seq = bufView.getShort( bufView.position() + this.SEQ_OFFSET );
		if( this.SEQ_LEN==4 )
			seq = bufView.getInt( bufView.position() + this.SEQ_OFFSET );
		byte[] packet = new byte[ packLen ];
		bufView.get( packet );
		CapPacket request = new CapPacketImpl(
				(InetSocketAddress)channel.socket().getRemoteSocketAddress(),
				packLen,
				seq,
				packet);
		
		return request;
	}
	
	@Override
	public InetSocketAddress getRemoteSocketAddress() throws IOException{
		try {
			InetSocketAddress result = (InetSocketAddress)channel.socket().getRemoteSocketAddress();
			return result;
		} catch (Exception e) {
			throw new IOException( "CapSession getRemoteSocketAddress error", e );
		}
	}
	
    public void setLogger(String logName){
		if( logName!=null && logName.length()>0 ){
			this.logger = Logger.getLogger(logName);
		}else{
			this.logger = null;
		}
    }
	private class WorkThread implements Runnable {
		CapPacket packet;
		WorkThread(CapPacket packet){
			this.packet = packet;
		}
		public void run(){
        	long start = System.currentTimeMillis();
        	byte[] front = {};
        	int len=0;
			try {
				len = packet.getPacket().length;
				handler.packetReceived(CapSessionImpl.this, packet);
				if( logger!=null ){
		        	front = getFrontBytes(packet.getPacket(),10);
					logger.info(packet.getAddress().getAddress().getHostAddress() + "|succ|" + (System.currentTimeMillis()-start) + "|" + HexUtil
							.bytes2HexStr(front) + "|" + len);
				}
			} catch (Throwable e) {//非网络异常不要断开连接
				e.printStackTrace();
				if( logger!=null ){
		        	front = getFrontBytes(packet.getPacket(),10);
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

	void setRemoteSocketDesc(SocketChannel channel) {
		try {
			Socket socket = channel.socket();
			InetSocketAddress address = (InetSocketAddress) socket.getRemoteSocketAddress();
			this.remoteSocketDesc = address.getAddress().getHostAddress() + ":" + address.getPort();
		} catch (Throwable e) {
			debugLog.warn("setRemoteSocketDesc", e);
		}
	}

	@Override
	public String getRemoteSocketDesc() {
		return remoteSocketDesc;
	}
}
