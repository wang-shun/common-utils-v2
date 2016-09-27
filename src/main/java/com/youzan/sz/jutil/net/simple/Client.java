package com.youzan.sz.jutil.net.simple;

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

public class Client {
	private InetSocketAddress addr;
	private SessionHandler handler;
	private Executor executor;
	private SessionImpl session;
	private              boolean open     =true;//false则客户端关闭，断开连接，不再重连
    private static final Logger  debugLog = Logger.getLogger("jutil");
	//初始化selector
	private static Selector selector;
	private static SelectThread selectThread;
	static{
		try {
			selector = Selector.open();
			selectThread = new SelectThread();
			selectThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Client( InetSocketAddress addr,SessionHandler handler, Executor executor, Map props) throws IOException{
		this.addr = addr;
		this.handler = handler;
		this.executor = executor;
    	reInit();
    	if( session==null ){//当前太多连接请求，请求队列已经满了，第一次就连不上
    		throw new IOException("SimpleClient connect queue is full");
    	}
	}
    	
    private synchronized void reInit(){
    	if( !open ){
    		return;
    	}
    	if( session!=null && session.getState() != Session.SessionStatus.CLOSED ){//已经发出重连请求了
    		return;
    	}
		//开启新链接
    	SessionImpl old = session;
    	SessionImpl tmp = new SessionImpl( selector,handler,executor);
    	tmp.status = Session.SessionStatus.NOT_CONNECTED;
    	if( old!=null )
    		tmp.logger = old.logger;
    	try {
        	if( !selectThread.asyncConnect( tmp,addr ) ){
        		throw new IOException("SimpleClient connect queue is full");
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
        		throw new IOException("SimpleClient disconnect queue is full, status is "+session.status);
        	}
        	open = false;
        	selector.wakeup();
    	}
    }
    public boolean send( int seq, byte[] bs) throws IOException {
    	if( !open ){
    		throw new IOException("SimpleClient is shutdown");
    	}
    	if( session.status != Session.SessionStatus.CLOSED  ){//未连接，或者已经连接
    		try {
        		session.write( seq, bs );
        		return true;
			} catch (IOException e) {
    			throw new IOException("SimpleClient write error: " + e.getMessage());
			}
    	}else{//重连
    		reInit();
    		throw new IOException("SimpleClient is reconnecting");
    	}
    }  
    public void sendOnly( int seq, byte[] bs) throws IOException {
    	if( !open ){
    		throw new IOException("SimpleClient is shutdown");
    	}
    	if( session.status != Session.SessionStatus.CLOSED  ){//未连接，或者已经连接
    		try {
        		session.write( seq, bs );
			} catch (IOException e) {
    			throw new IOException("SimpleClient write error: " + e.getMessage());
			}
    	}else{//重连
    		reInit();
    		throw new IOException("SimpleClient is reconnecting");
    	}
    }    
	private static class RegisterTask{
		boolean register;
		SessionImpl obj;
		InetSocketAddress addr2;
		RegisterTask( boolean register, SessionImpl obj, InetSocketAddress addr2 ){
			this.register = register;
			this.obj = obj;
			this.addr2 = addr2;
		}
	}

	//主线程
	private static class SelectThread extends Thread{
		Queue<RegisterTask> queue = new ArrayBlockingQueue<RegisterTask>(1024);//待连接的SimpleClient
		boolean asyncConnect( SessionImpl obj, InetSocketAddress addr2 ){
			return queue.offer( new RegisterTask( true ,obj, addr2 ) );
		}
		boolean asyncDisconnect( SessionImpl obj ){
			return queue.offer( new RegisterTask( false ,obj, null ) );
		}
		void doRegister(){
			RegisterTask task = queue.poll();
			while( task!=null ){
				SessionImpl obj = task.obj;
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
	                    	debugLog.error("SimpleClient SelectionKey is not valid");
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
								SessionImpl attach = (SessionImpl) key.attachment();
								attach.status = Session.SessionStatus.CONNECTED;//修改标志位
							}
							if (key.isReadable()){
//								System.out.println("isReadable");
								SessionImpl attach = (SessionImpl) key.attachment();
								int n = attach.doRead();
//								System.out.println("read:"+n);
							}
							if( key.isWritable() ){
//								System.out.println("isWritable");
								SessionImpl attach = (SessionImpl) key.attachment();
								int n = attach.doWrite();
//								System.out.println("write:"+n);
							}
						} catch (Throwable e) {
							e.printStackTrace();
							SessionImpl attach = (SessionImpl) key.attachment();
							attach.close();
						}
					}
				}
			} catch (Throwable e) {
				System.out.println("SimpleClient crash at: " + System.currentTimeMillis());
				e.printStackTrace();
				debugLog.error( "SimpleClient main thread error: " + e.getMessage());
			}
		}//end run
	}
	public static void main(String[] args) throws IOException{
		SessionHandler handler = new SessionHandler(){
			public void packetReceived( Session session, Packet packet ){
				System.out.println( packet.getSize() + ":" + packet.getSeq() + ":" + packet.getBody().length);
			}
		};
		Executor executor = new ThreadPoolExecutor(5, 5, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
		Client client = new Client( new InetSocketAddress("127.0.0.1", 12345), handler, executor, null );
		client.setRecvPackageLogger("abc");
		for (int i = 10; i <= 20; i++) {
			try {
				Thread.sleep(1000);
				byte[] bs = new byte[1000*i];
				bs[0] = (byte)i;
				client.sendOnly( i, bs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
//		try {
//			Thread.sleep(1000);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		client.shutduwn();
	}
}
