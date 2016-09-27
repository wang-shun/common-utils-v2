package com.youzan.sz.jutil.net.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.qq.jutil.util.TimeoutDetector;
//import com.qq.jutil.util.TimeoutHandler;
//import com.qq.tac2.jdt.share.AsyncClientException;
//import com.qq.tac2.jdt.share.AsyncClientInfoBox;
//import com.qq.tac2.jdt.share.AsyncClientRuntimeInfo;

/**
 * 基于异步方式客户端Client类，不阻塞
 * 
 * @author albertyzhu
 * 
 */
public class AsyncClient {
    private Client          client;
    //	private RequestListManager requestManager;
    private SessionHandler  handler;
    private static Executor executor;
    static {
        executor = new ThreadPoolExecutor(1, 1, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20000));
    }

    /**
     * 创建同步客户端，会建立一个长连接。
     * 
     * @param addr
     * @throws IOException
     */
    public AsyncClient(InetSocketAddress addr, Map props) throws IOException {

    }
    //		this.requestManager = new RequestListManager();
    //		handler = new SessionHandler() {
    //			public void packetReceived(Session session, Packet packet) {
    //				int seq = packet.getSeq();
    //				byte[] body = packet.getBody();
    //				//System.out.println("---------------------------receive resp seq "+seq+" "+client.hashCode());
    //				AsyncRequestListItem item = requestManager.removeRequestItem(seq);
    //				if (item != null) {
    ////					item.box.setResult(body);
    ////					item.box.resumeRequest();
    //				} else {
    //					//System.out.println("---------------------------can not find receive resp seq "+seq+" "+client.hashCode());
    //				}
    //			}
    //		};
    //		client = new Client(addr, handler, executor, props);
    //	}

    //	private static final AsyncClientRuntimeInfo empytAsyncVar = new AsyncClientRuntimeInfo() {
    //		public boolean run(AsyncClientInfoBox __box__) {
    //			__box__.setContinueRun(true);
    //			return false;
    //		}
    //	};
    //
    //	public boolean sendOnly(AsyncClientInfoBox box ,byte[] bs,int timeout ) throws IOException {
    //
    //		int seq = requestManager.getNextSeq();
    //		AsyncRequestListItem item = new AsyncRequestListItem();
    //		item.box = box;
    //		item.timeout = timeout;
    //		boolean result = requestManager.addIfAbsent(seq, item);
    //		if (!result)
    //			throw new IOException("BlockingClient addIfAbsent error:" + seq);
    //		box.suspendRequest();
    //		box.setAsyncVariables(empytAsyncVar);
    //		box.setContinueRun(false);
    //		boolean b = false ;
    //		try {
    //			b =client.send(seq, bs);
    //		} catch (Throwable e) {
    //			box.unSuspend();
    //		    box.setContinueRun(true);
    //		      throw new AsyncClientException("sendAscyncMsgFailed ",e) ;
    //		}
    //		box.setAsyncCallReturnCode(true);
    //		//System.err.println("---------------------------call asyncClient "+client.hashCode()+" sendOnly "+seq+" "+b);
    //		if ( !b ) {
    //			throw new IOException("can not send msg ");
    //		}
    //		return true;
    //	}
    //
    //	public void shutduwn() throws IOException {
    //		client.shutduwn();
    //	}
    //
    //	static class AsyncRequestListItem {
    //		AsyncClientInfoBox box; // 异步模式下回调的上下文
    //		int timeout;
    //	}
    //
    //	static class RequestListManager {
    //
    //		static ConcurrentHashMap<Integer,AsyncRequestListItem> futuremap = new ConcurrentHashMap<Integer,AsyncRequestListItem>(4086);
    //
    //		static TimeoutDetector<Integer> td = new TimeoutDetector<Integer>();
    //
    //		private static AtomicInteger seqID = new AtomicInteger();
    //
    //		static AtomicBoolean inited = new AtomicBoolean(false) ;
    //
    //		static Object lock = new Object();
    //
    //
    //		public RequestListManager() {
    //		}
    //
    //		public AsyncRequestListItem getRequestItem(int seq) {
    //			return futuremap.get(seq);
    //		}
    //
    //		public AsyncRequestListItem removeRequestItem(int seq) {
    //			return futuremap.remove(seq);
    //		}
    //
    //		public boolean addIfAbsent(int seq, AsyncRequestListItem item) {
    //			AsyncRequestListItem old = futuremap.putIfAbsent(seq, item);
    //			if (old == null) {
    //				td.addWithTimeout(seq, item.timeout);
    //				if ( !inited.get() ) {
    //					synchronized(lock) {
    //						if ( !inited.get() ) {
    //							inited.set(true);
    //							td.startHandleThread(new TimeoutHandler<Integer>(){
    //								public void handle(Integer o, long expireTime) {
    //									if ( futuremap.containsKey(o) ) {
    //										AsyncRequestListItem f  = futuremap.remove(o);
    //										f.box.setException(new  AsyncClientException("async request timeout. expireTime " + (System.currentTimeMillis()-expireTime) ));
    //										f.box.resumeRequest();
    //									}
    //								}
    //							});
    //						}
    //					}
    //					}
    //				return true;
    //			}
    //			else
    //				return false;
    //		}

    //		public void deleteRequestMessage(int seq) {
    //			futuremap.remove(seq);
    //		}
    //
    //		/**
    //		 * server端产生唯一序列号
    //		 *
    //		 * @return
    //		 */
    //		public int getNextSeq() {
    //			int i = seqID.getAndIncrement();
    //			return i & Integer.MAX_VALUE;
    //		}

    //	}

    public static void main(String[] args) throws IOException {

    }
}
