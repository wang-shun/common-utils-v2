package com.youzan.sz.jutil.net.cap;

import com.youzan.sz.jutil.j4log.Logger;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.Logger;

/**
 * 处理读写事件的selector池
 * 
 * @author kenwaychen
 * 
 */
class ReactorPool {
	
	private static final Logger debugLog = Logger.getLogger("CapServer");

	private static final int processors = Runtime.getRuntime().availableProcessors();

	/**
	 * 默认启动CPU + 1个selector
	 * 
	 * coreSelectorSize = cpus > 8 ? 4 + cpus * 5 / 8 : cpus + 1;
	 * 
	 */
	private static final int DEFAULT_SIZE = processors > 8 ? 4 + (processors * 5 / 8) : processors + 1;

	/**
	 * selector池
	 */
	private Reactor[] reactorPool = null;

	/**
	 * 序列号生成器
	 */
	private AtomicInteger seqGenerator = new AtomicInteger(0);

	public ReactorPool() {
		this(DEFAULT_SIZE);
	}

	public ReactorPool(int size) {
		// 初始化elector池
		reactorPool = new Reactor[size];
		debugLog.info("[ReactorPool] cpus=" + processors + " ,reactorPool size=" + DEFAULT_SIZE);
		for (int i = 0; i < reactorPool.length; i++) {
			// 初始化selector
			reactorPool[i] = new Reactor(i);
			reactorPool[i].start();
			debugLog.info("[ReactorPool] " + reactorPool[i].getName() + " is running!");
		}
	}

	/**
	 * 注册读事件
	 * 
	 * @param channel
	 * @param attachment
	 * @throws ClosedChannelException
	 */
	public void dispatch(SocketChannel channel, CapSessionHandler handler, Executor executor, CapDef capDef,
			CapRejectedExecutionHandler rejectedExecutionHandler) throws ClosedChannelException {
		// 构造附件
		int seqID = getNextSeq();
		CapSessionImpl sessionImpl = new CapSessionImpl(handler, executor, capDef, rejectedExecutionHandler, null);
		Reactor reactor = getReactor(seqID);
		reactor.handle(SelectionKey.OP_READ, channel, sessionImpl);
		sessionImpl.setRemoteSocketDesc(channel);
	}

	/**
	 * 获取一个Reactor
	 * 
	 * @param seqId
	 * @return
	 */
	private Reactor getReactor(int seqId) {
		int index = seqId % reactorPool.length;
		return reactorPool[index];
	}

	/**
	 * 获取唯一的序列号，理论上没有重复的可能
	 * 
	 * @return
	 */
	private int getNextSeq() {
		return (seqGenerator.incrementAndGet() & Integer.MAX_VALUE);
	}

}
