package com.youzan.sz.jutil.net.cap;

import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.qq.jutil.j4log.Logger;

public class CapClient {
	private InetSocketAddress addr;
	private CapSessionHandler handler;
	private Executor executor;
	private CapSessionImpl session;
	private boolean open = true;// false则客户端关闭，断开连接，不再重连
	private CapDef capDef;
	private static final Logger debugLog = Logger.getLogger("CapClient");
	// 初始化selector
	private static Selector selector;
	private static SelectThread selectThread;
	private static String requestAddr;
	// 用于线程池满的时候框架回调业务的接口，默认为一个空实现
	private CapRejectedExecutionHandler rejectedExecutionHandler = new DefaultCapRejectedExecutionHandler();

	static {
		try {
			selector = Selector.open();
			selectThread = new SelectThread();
			selectThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public CapClient( InetSocketAddress addr,CapSessionHandler handler, Executor executor, CapDef capDef, Map props) throws IOException{
		this.addr = addr;
		requestAddr = (addr == null ? "" : addr.toString());
		this.handler = handler;
		this.executor = executor;
		this.capDef = capDef;
    	reInit();
    	if( session==null ){//当前太多连接请求，请求队列已经满了，第一次就连不上
    		throw new IOException("CapClient connect queue is full");
    	}
	}
    	
    private synchronized void reInit(){
    	if( !open ){
    		return;
    	}
    	if( session!=null && session.getState() != CapSession.CapSessionStatus.CLOSED ){//已经发出重连请求了
    		return;
    	}
		//开启新链接
		CapSessionImpl old = session;
		CapSessionImpl tmp = new CapSessionImpl(handler, executor, capDef, rejectedExecutionHandler, null);
    	tmp.status = CapSession.CapSessionStatus.NOT_CONNECTED;
    	if( old!=null )
    		tmp.logger = old.logger;
    	try {
        	if( !selectThread.asyncConnect( tmp,addr ) ){
        		throw new IOException("CapClient connect queue is full");
        	}
        	session = tmp;
        	selector.wakeup();
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    synchronized void setRecvPackageLogger(String logName){
    	session.setLogger(logName);
    }
    public synchronized void shutduwn() throws IOException{
    	if( open ){
        	if( !selectThread.asyncDisconnect( session ) ){
        		throw new IOException("CapClient disconnect queue is full, status is "+session.status);
        	}
        	open = false;
        	selector.wakeup();
    	}
    }
    public void sendOnly( int seq,byte[] packet,int pos,int len ) throws IOException {
    	if( !open ){
    		throw new IOException("CapClient is shutdown");
    	}
    	if( session.status == CapSession.CapSessionStatus.CONNECTED  ){//已经连接才发消息
    		try {
        		session.write( seq, packet,pos,len );
			} catch (IOException e) {
    			throw new IOException("CapClient write error: " + e.getMessage());
			}
    	}else{//重连
    		reInit();
    		throw new IOException("CapClient is reconnecting to " + addr.getAddress().getHostAddress() + ":" + addr.getPort());
    	}
    	
    }
    public void sendOnly( int seq,byte[] packet ) throws IOException {
    	this.sendOnly(seq, packet,0,packet.length);
    }
    private static class RegisterTask{
		boolean register;
		CapSessionImpl obj;
		InetSocketAddress addr2;
		RegisterTask( boolean register, CapSessionImpl obj, InetSocketAddress addr2 ){
			this.register = register;
			this.obj = obj;
			this.addr2 = addr2;
		}
	}

	//主线程
	private static class SelectThread extends Thread {
		Queue<RegisterTask> queue = new ArrayBlockingQueue<RegisterTask>(1024);//待连接的CapClient
		boolean asyncConnect( CapSessionImpl obj, InetSocketAddress addr2 ){
			return queue.offer( new RegisterTask( true ,obj, addr2 ) );
		}
		boolean asyncDisconnect( CapSessionImpl obj ){
			return queue.offer( new RegisterTask( false ,obj, null ) );
		}
		void doRegister(){
			RegisterTask task = queue.poll();
			while( task!=null ){
				CapSessionImpl obj = task.obj;
				boolean register = task.register;
				try {
					if( register ){
			    		SocketChannel ch = SocketChannel.open();
			        	ch.configureBlocking(false);
			        	ch.connect( task.addr2 );
			        	SelectionKey k = ch.register(selector, SelectionKey.OP_CONNECT);
			        	obj.key = k;
			        	obj.channel = ch;
			        	k.attach( obj );
					}else{
						obj.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					obj.close();
				}
				task = queue.poll();
			}
		}
		@Override
		public void run() {
			try {
				while( true ){
					doRegister();
					int num = selector.select();
//					System.out.println("num="+num);
					Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
					while (iter.hasNext()){
						SelectionKey key = iter.next();
						iter.remove();
	                    if( !key.isValid() ){
	                        System.err.println("CapClient SelectionKey is not valid");
	                        continue;
	                    }
						try {
							if ( key.isConnectable() ){
//								System.out.println("isConnectable");
								SocketChannel channel = (SocketChannel) key.channel();
								if (channel.isConnectionPending()){
									channel.finishConnect();
								}
								key.interestOps( SelectionKey.OP_READ | SelectionKey.OP_WRITE );
								CapSessionImpl attach = (CapSessionImpl) key.attachment();
								attach.status = CapSession.CapSessionStatus.CONNECTED;//修改标志位
							}
							if (key.isReadable()){
//								System.out.println("isReadable");
								CapSessionImpl attach = (CapSessionImpl) key.attachment();
								int n = attach.doRead();
//								System.out.println("read:"+n);
							}
							if( key.isWritable() ){
//								System.out.println("isWritable");
								CapSessionImpl attach = (CapSessionImpl) key.attachment();
								int n = attach.doWrite();
//								System.out.println("write:"+n);
							}
						} catch (Throwable e) {
							e.printStackTrace();
							CapSessionImpl attach = (CapSessionImpl) key.attachment();
							attach.close();
							debugLog.error("CapClient(" + requestAddr + ") SelectionKey error: " + e.getMessage());
						}
					}
				}
			} catch (Throwable e) {
				System.out.println("CapClient crash at: " + System.currentTimeMillis());
				e.printStackTrace();
				debugLog.error( "CapClient(" + requestAddr + ") main thread error: " + e.getMessage());
			}
		}//end run
	}

	public static void main(String[] args) throws IOException {
//		SessionHandler handler = new SessionHandler(){
//			public void packetReceived( Session session, Packet packet ){
//				System.out.println( packet.getSize() + ":" + packet.getSeq() + ":" + packet.getHead().length + ":" + packet.getBody().length + ":" + packet.getTail().length);
//			}
//		};
//		Executor executor = new ThreadPoolExecutor(5, 5, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
//		Client client = new Client( new InetSocketAddress("127.0.0.1", 12345), handler, executor, null );
//		client.setRecvPackageLogger("abc");
//		for (int i = 1; i <= 100; i++) {
//			try {
//				Thread.sleep(1000);
//				byte[] bs = new byte[1000*i];
//				bs[0] = (byte)i;
//				client.sendOnly( i, bs);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		//以Pverify的协议为例
		CapSessionHandler handler = new CapSessionHandler(){
			public void packetReceived( CapSession session, CapPacket packet ){
				System.out.println( packet.getSize() + ":" + packet.getSeq() + ":" + packet.getPacket().length);
//				System.out.println( HexUtil.bytes2HexStr(packet.getHead()));
//				System.out.println( HexUtil.bytes2HexStr(packet.getBody()));
//				System.out.println( HexUtil.bytes2HexStr(packet.getTail()));
			}
		};
		Executor executor = new ThreadPoolExecutor(5, 5, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
		CapDef capDef = new CapDef(1,2,7,2);
		CapClient client = new CapClient( new InetSocketAddress("127.0.0.1", 12345), handler, executor, capDef, null );
		client.setRecvPackageLogger("abc");
		while(true){
			for (int i = 1; i <= 60; i++) {
				try {
					Thread.sleep(100);
					client.sendOnly(i,new byte[1000*i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
//				if (i == 10)
//					System.exit(1);
			}
		}
		
	}
}
