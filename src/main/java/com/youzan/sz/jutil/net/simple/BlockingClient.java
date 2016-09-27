package com.youzan.sz.jutil.net.simple;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.tac2.jdt.share.AsyncClientInfoBox;
//import com.qq.tac2.jdt.share.AsyncClientRuntimeInfo;

/**
 * 基于异步方式客户端Client类，模拟同步的方式
 * 
 * @author junehuang
 * 
 */
public class BlockingClient {
	private Client client;
	private RequestListManager requestManager;
	private SessionHandler handler;
	private static Executor executor;
	static {
		executor = new ThreadPoolExecutor(1, 1, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20000));
	}

	AsyncClient asyncClient;

	/**
	 * 创建同步客户端，会建立一个长连接。
	 * 
	 * @param addr
	 * @throws IOException
	 */
	public BlockingClient(InetSocketAddress addr, Map props) throws IOException {
		this.requestManager = new RequestListManager();
		handler = new SessionHandler() {
			public void packetReceived(Session session, Packet packet) {
				int seq = packet.getSeq();
				byte[] body = packet.getBody();
				RequestListItem item = requestManager.getRequestItem(seq);
				if (item != null) {
					item.attachObject = body;
					item.latch.countDown();
				}
			}
		};
		client = new Client(addr, handler, executor, props);
		asyncClient = new AsyncClient(addr, props);
	}

	/**
	 * 发送，阻塞，接收
	 * 
	 * @param bs
	 * @param timeout
	 *            MILLISECONDS
	 * @return
	 * @throws IOException
	 */
	public byte[] sendAndReceive(byte[] bs, int timeout) throws IOException {
		int seq = requestManager.getNextSeq();
		CountDownLatch latch = new CountDownLatch(1);
		RequestListItem item = new RequestListItem(latch);
		boolean result = requestManager.addIfAbsent(seq, item);
		if (!result)
			throw new IOException("BlockingClient addIfAbsent error:" + seq);
		try {
			client.sendOnly(seq, bs);
			if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
				return item.attachObject;
			} else {
				throw new SocketTimeoutException("BlockingClient timeout error");
			}
		} catch (InterruptedException e) {
			throw new IOException("BlockingClient await error");
		} finally {
			requestManager.deleteRequestMessage(seq);
		}
	}

//	class Pair implements AsyncClientRuntimeInfo {
//		byte[] bs;
//		int timeout;
//		int __executePointer__;
//
//		public boolean run(AsyncClientInfoBox box) {
//			try {
//				box.setContinueRun(true);
//				box.setAsyncCallReturnCode(false);
//				return false;
//			} catch (RuntimeException e) {
//				throw e;
//			} catch (Throwable e) {
//				throw new com.qq.tac2.jdt.share.AsyncClientException("async call error: ", e);
//			}
//		}
//	}
//
//	/**
//	 * 异步发送 不阻塞 等待box唤醒回调
//	 *
//	 * @param box
//	 * @param bs
//	 * @param timeout
//	 * @return
//	 * @throws IOException
//	 */
//	public boolean tacAsync_sendAndReceive(AsyncClientInfoBox box, byte[] bs, int timeout) throws IOException {
//		Pair pr = new Pair();
//		pr.bs = bs;
//		pr.timeout = timeout;
//		box.setAsyncVariables(pr);
//		try {
//			box.enterAsyncCall();
//			boolean __asyncflag__;
//			final com.qq.tac2.jdt.share.AsyncClientRuntimeInfo __ri__ = box.getAsyncVariables();
//			if (__ri__ == null) {
//				__asyncflag__ = asyncClient.sendOnly(box, pr.bs, pr.timeout);
//			} else {
//				__asyncflag__ = __ri__.run(box);
//			}
//			if (__asyncflag__) {
//				box.setAsyncCallReturnCode(true);
//				return true;
//			} else {
//				box.finishAsyncCall();
//			}
//		} finally {
//			box.exitAsyncCall();
//		}
//		box.setAsyncCallReturnCode(false);
//		return false;
//	}

	public void sendOnly(byte[] bs) throws IOException {
		int seq = requestManager.getNextSeq();
		client.sendOnly(seq, bs);
	}

	public void shutduwn() throws IOException {
		client.shutduwn();
	}

	static class RequestListItem {
		volatile byte[] attachObject; // 附加的数据，接受到应答以后存放处理结果
		final CountDownLatch latch; // 接收到应答后，将await这个latch的线程唤醒

		public RequestListItem(CountDownLatch latch) {
			this.latch = latch;
		}
	}

	static class RequestListManager {
		private AtomicInteger seqID = new AtomicInteger();

		private ConcurrentHashMap<Integer, RequestListItem> map = new ConcurrentHashMap<Integer, RequestListItem>(256);

		public RequestListManager() {
		}

		public RequestListItem getRequestItem(int seq) {
			return map.get(seq);
		}

		public boolean addIfAbsent(int seq, RequestListItem item) {
			RequestListItem old = map.putIfAbsent(seq, item);
			if (old == null)
				return true;
			else
				return false;
		}

		public void deleteRequestMessage(int seq) {
			map.remove(seq);
		}

		/**
		 * server端产生唯一序列号
		 * 
		 * @return
		 */
		public int getNextSeq() {
			int i = seqID.getAndIncrement();
			return i & Integer.MAX_VALUE;
		}

	}

	public static void main(String[] args) throws IOException {
		BlockingClient client = new BlockingClient(new InetSocketAddress("127.0.0.1", 12345), null);
		try {
			for (int i = 0; i < 10; i++) {
				try {
					System.out.println((client.sendAndReceive(new byte[] { (byte) i }, 2000))[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
