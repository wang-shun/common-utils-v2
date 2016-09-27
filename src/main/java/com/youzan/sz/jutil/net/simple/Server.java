package com.youzan.sz.jutil.net.simple;

import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.qq.jutil.j4log.Logger;
/**
 * 
 * 本客户端服务器框架实现了网络和应用分离，框架本身实现了网络部分。
 * 客户端和服务器的数据包必须遵循“简单网络协议”形式，也就是size(4)+seq(4)+body
 * 线程池和handle（包括应用层协议编码解码）需要开发人员自己来写。
 * @author junehuang
 *
 */
public class Server extends Thread{
	private InetSocketAddress addr;
	private SessionHandler handler;
	private Executor executor;
    private Selector selector;
    private static final Logger debugLog = Logger.getLogger("jutil");
    private Logger logger;

    /**
	 * 唯一的构造函数
	 * @param addr 可以为null，null表示随机绑定一个端口，cool
	 * @param threadPool 处理各种事件的线程池，现在只处理收包的事件，你可以想象成resin的线程池，不断在回调servlet
	 * @param handler 你可以想象成一个servlet，被线程池回调
	 */
	public Server( InetSocketAddress addr, SessionHandler handler, Executor executor, Map props ){
		this.addr = addr;
		this.handler = handler;
		this.executor = executor;
	}
	@Override
	public void run(){
		try {
			//init selector
			selector = getSelector( addr );
			System.out.println("SimpleServer is running......");

			//handleKey
			for (;;) {
				selector.select();
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
//				System.out.println("selector size : "+selector.keys().size());
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();
                    if( !key.isValid() ){
                        System.err.println("SelectionKey is not valid");
                        continue;
                    }					
					try {
						handleKey(key);
					} catch (Throwable e) {//网络异常要关闭连接
						e.printStackTrace();
						try {
							SessionImpl attach = (SessionImpl) key.attachment();
							attach.close();
						} catch (Exception e2) {
							try {
								key.channel().close();
							} catch (Throwable e3) {
								e3.printStackTrace();
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			System.out.println("SimpleServer crash at: " + System.currentTimeMillis());
			e.printStackTrace();
			debugLog.error( "SimpleServer main thread error: " + e.getMessage());
		}
	}
	// 处理事件
	private void handleKey(SelectionKey key) throws IOException {
		if (key.isAcceptable()) { // 接收请求
//			System.out.println("isAcceptable");
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			SocketChannel channel = server.accept();
			channel.configureBlocking(false);
			SelectionKey k = channel.register(selector, SelectionKey.OP_READ );
			SessionImpl obj = new SessionImpl( selector,handler,executor );
        	obj.key = k;
        	obj.channel = channel;
        	obj.status = Session.SessionStatus.CONNECTED;
        	obj.logger = this.logger;
			k.attach( obj );
		}
		if (key.isReadable()) { // 读数据
//			System.out.println("isReadable");
			SessionImpl attach = (SessionImpl) key.attachment();
			if( attach == null ){
				throw new RuntimeException("attach is null when writing...");
			}
			int readCount = attach.doRead( );
//			System.out.println("readCount: "+readCount);
		}
		if (key.isWritable()) { // 写数据
//			System.out.println("isWritable");
			SessionImpl attach = (SessionImpl) key.attachment();
			if( attach == null ){
				throw new RuntimeException("attach is null when writing...");
			}
			int writeCount = attach.doWrite( );
//			System.out.println("writeCount: "+writeCount);
		}
	}
	
    void setRecvPackageLogger(String logName){
		if( logName!=null && logName.length()>0 ){
			this.logger = Logger.getLogger(logName);
		}else{
			this.logger = null;
		}
    }
	
	// 获取Selector
	private static Selector getSelector(InetSocketAddress addr) throws IOException{
		ServerSocketChannel server = ServerSocketChannel.open();
		Selector sel = Selector.open();
		server.socket().bind( addr,1024 );//backlog大一点，免得建立连接的速度大于accept的速度出现连接请求拒绝
		server.configureBlocking(false);
		server.register(sel, SelectionKey.OP_ACCEPT);
		return sel;
	}
	
	public static void main(String[] args) {
		SessionHandler handler = new SessionHandler(){
			public void packetReceived( Session session, Packet packet ){
				try {
					session.write(packet.getSeq(), packet.getBody());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Executor threadPool = new ThreadPoolExecutor(50, 50, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(2000));
		Server server = new Server( new InetSocketAddress(12345), handler, threadPool, null );
//		server.setRecvPackageLogger("SimpleAccessLog");
		server.start();
	}

}
