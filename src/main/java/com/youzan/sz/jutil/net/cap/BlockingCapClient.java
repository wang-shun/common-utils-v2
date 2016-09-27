package com.youzan.sz.jutil.net.cap;

import com.youzan.sz.jutil.crypto.HexUtil;

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

public class BlockingCapClient {
	private CapClient client;
	private RequestListManager requestManager;
	private CapSessionHandler handler;
	private static Executor executor;
	static {
		executor = new ThreadPoolExecutor(1, 1, 120, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20000));
	}
	
	public BlockingCapClient( InetSocketAddress addr, CapDef capDef, Map props ) throws IOException{
		this.requestManager = new RequestListManager( capDef );
		handler = new CapSessionHandler() {
			public void packetReceived( CapSession session, CapPacket packet ){
				int seq = packet.getSeq();
				byte[] body = packet.getPacket();
				RequestListItem item = requestManager.getRequestItem(seq);
				if (item != null) {
					item.attachObject = body;
					item.latch.countDown();
				}
			}
		};
		client = new CapClient(addr, handler, executor, capDef, props);
	}
	
	public byte[] sendAndReceive(byte[] bs, int timeout) throws IOException {
	    return sendAndReceive(bs, timeout, 0, bs.length);
	}
	public byte[] sendAndReceive(byte[] bs, int timeout, int pos,int len) throws IOException {
		int seq = requestManager.getNextSeq();
		CountDownLatch latch = new CountDownLatch(1);
		RequestListItem item = new RequestListItem(latch);
		boolean result = requestManager.addIfAbsent(seq, item);
		if (!result)
			throw new IOException("BlockingCapClient addIfAbsent error:" + seq);
		try {
			client.sendOnly(seq, bs, pos, len);
			if (latch.await(timeout, TimeUnit.MILLISECONDS)) {
				return item.attachObject;
			} else {
				throw new SocketTimeoutException("BlockingCapClient timeout error");
			}
		} catch (InterruptedException e) {
			throw new IOException("BlockingCapClient await error");
		} finally {
			requestManager.deleteRequestMessage(seq);
		}
	}
	public void sendOnly(byte[] bs) throws IOException {
		sendOnly(bs, 0, bs.length);
	}
	public void sendOnly(byte[] bs, int pos,int len) throws IOException {
        int seq = requestManager.getNextSeq();
        client.sendOnly(seq, bs, pos, len);
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

		private final int SEQ_LEN;//协议中序列号的长度，只可能是2或者4
		
		public RequestListManager(CapDef capDef) {
			this.SEQ_LEN = capDef.SEQ_LEN;
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
			if( this.SEQ_LEN == 2)
				return i & Short.MAX_VALUE;//因为cap协议很变态，可能seq只有2个字节，这个时候回包时候序列号可能对不上。
			else
				return i & Integer.MAX_VALUE;
		}
	}
	
	public static void main(String[] args) throws IOException{
		BlockingCapClient client = new BlockingCapClient(new InetSocketAddress("127.0.0.1", 12345), new CapDef(1,2,7,2), null);
		try {
			for (int i = 0; i < 10; i++) {
				byte[] bs = new byte[10];
				bs[9] = (byte)i;
				try {
					System.out.println( HexUtil.bytes2HexStr( (client.sendAndReceive(bs, 2000)) ) );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			client.shutduwn();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
