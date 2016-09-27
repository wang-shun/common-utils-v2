package com.youzan.sz.jutil.net.cap;

import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

//import com.qq.jutil.j4log.Logger;

class Reactor extends Thread {

	private static final Logger debugLog = Logger.getLogger("CapServer");

	private Selector selector = null;

	private BlockingQueue<RegisterEvent> queue = null;

	public Reactor(int index) {
		super("CapServer-Reactor-" + index);
		try {
			this.selector = Selector.open();
			this.queue = new LinkedBlockingQueue<RegisterEvent>();
		} catch (IOException e) {
			throw new RuntimeException("Reactor open error!");
		}
	}

	public void handle(int key_op, SocketChannel channel, CapSessionImpl sessionImpl) throws ClosedChannelException {
		RegisterEvent event = new RegisterEvent(key_op, channel, sessionImpl);
		// 无界队列，不可能满的
		queue.offer(event);
		selector.wakeup();
	}

	@Override
	public void run() {
		try {
			while (true) {
				doRegister();
				int selected = selector.select();
				if (0 == selected)
					continue;
				Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();
					iterator.remove();
					try {
						process0(key);
					} catch (Throwable e) {
						debugLog.error("[Reactor] process", e);
						// 网络异常，尝试关闭该网络连接
						try {
							((CapSessionImpl) key.attachment()).close();
						} catch (Throwable e2) {
							debugLog.warn("[Reactor] SessionImpl.close()", e2);
						}
					}
				}
			}
		} catch (Throwable e) {
			// selector 发生严重错误
			debugLog.fatal("[Reactor] " + this.getName() + " crash !");
		}
	}

	private void process0(SelectionKey key) throws IOException {
		boolean isProcessed = false;
		if (key.isValid() && key.isAcceptable()) {
			// 不可能发生，因为没有注册Acceptable事件
			debugLog.warn("[Reactor] I can't believe it happend! key.isValid()=" + key.isValid() + " key.isAcceptable()=" + key.isAcceptable());
		}
		if (key.isValid() && key.isReadable()) {
			// 可读
			((CapSessionImpl) key.attachment()).doRead();
			isProcessed = true;
		}
		if (key.isValid() && key.isWritable()) {
			// 可写
			((CapSessionImpl) key.attachment()).doWrite();
			isProcessed = true;
		}
		if (!isProcessed) {
			throw new IOException("[Reactor] key error: " + key.isValid());
		}
	}

	private void doRegister() {
		RegisterEvent event = null;
		if (selector.keys().isEmpty()) {
			// 如果当前selector的keys是空的，则保证一定拿到一个事件
			while (null == event) {
				try {
					event = queue.take();
					if (!doRegister0(event))
						// 如果这个事件注册不成功，则等待下一个，保证select的时候至少有一个成功的事件
						event = null;
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		// 把队列里面的全部注册完
		while ((event = queue.poll()) != null) {
			doRegister0(event);
		}
	}

	private boolean doRegister0(RegisterEvent event) {
		try {
			int key_op = event.key_op;
			SocketChannel channel = event.channel;
			CapSessionImpl sessionImpl = event.sessionImpl;
			SelectionKey key = channel.register(selector, key_op, sessionImpl);
			/**
			 * 以下是debug信息，看看这种机制有没有问题
			 */
			long createTime = event.createTime;
			long cost = System.currentTimeMillis() - createTime;
			if (cost > 1000) {
				// 超过1秒没有处理的连接请求，打LOG
				debugLog.warn("[Reactor] doRegister0() cost " + cost);
			}
			sessionImpl.status = CapSession.CapSessionStatus.CONNECTED;
			sessionImpl.channel = channel;
			sessionImpl.key = key;
			return true;
		} catch (Throwable e) {
			// 管道被关闭了，一般来说不可能，也不需要额外的处理
			return false;
		}
	}

	private class RegisterEvent {

		private int key_op = -1;

		private SocketChannel channel = null;

		private CapSessionImpl sessionImpl = null;

		private long createTime = 0l;

		public RegisterEvent(int key_op, SocketChannel channel, CapSessionImpl sessionImpl) {
			this.key_op = key_op;
			this.channel = channel;
			this.sessionImpl = sessionImpl;
			this.createTime = System.currentTimeMillis();
		}

	}

}
