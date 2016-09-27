package com.youzan.sz.jutil.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PreDestroy;

/**
 * 使用了一个锁来实现:因为对MappedByteBuffer进行增加、移除数据没办法原子性操作.使用Actomic会使算法复杂化
 * 放入队列中的元素必须要实现java.io.Serializable接口
 * 算法实现与PersistenQueue类似，参考ArrayBlockingQueue. 与PersistenQueue不同之处：
 * 1、实现了BlockingQueue、Serializable接口,并优化了算法,缩小了锁的使用范围
 * 2、优化扩容算法，通过记录放入数据的大小的平均值(expectObjectSize)和expectNum,合理的进行扩容
 * 3、增加了javax.annotation.PreDestroy注解。JVM正常退出时将MappedByteBuffer中的数据force()到硬盘中
 * 4、应尽量使用BlockingQueue接口的方法
 * 
 * @author qingquanwu
 * 
 * @param <E>
 */
public class PersistenceBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {

	/**
	 * @param klass
	 *            Class
	 * @param maxSize
	 *            最大长度
	 * @param queuePath
	 *            映射文件所在路径
	 */
	public static <E> PersistenceBlockingQueue<E> getInstance(Class<E> klass, int maxSize, String queuePath) {
		return new PersistenceBlockingQueue<E>(maxSize, queuePath);
	}

	/**
	 * @param klass
	 *            Class
	 * @param maxSize
	 *            最大长度
	 * @param exceptNum
	 *            扩容因子(扩容时新增映射空间大小=平均对象大小*exceptNum，具体算法resize())
	 * @param queuePath
	 *            映射文件所在路径
	 */
	public static <E> PersistenceBlockingQueue<E> getInstance(Class<E> klass, int maxSize, int expectNum, String queuePath) {
		return new PersistenceBlockingQueue<E>(maxSize, expectNum, queuePath);
	}

	private static final long serialVersionUID = -2050255490798665480L;

	/** 队列信息容器的长度 */
	private static final int QUEUE_INFO_LENGTH = 16;
	/** 队列信息容器 */
	private MappedByteBuffer queueInfoMapped;
	private IntBuffer queueInfoBuffer;// head|tail|count|size

	private int head, tail, count, size;

	private final int maxSize;

	/** 队列的路径 */
	private final String queuePath;
	/** 存放队持久化数据容器 */
	private MappedByteBuffer queueBuffer;

	/** Main lock guarding all access */
	private final Lock lock;
	/** Condition for waiting takes */
	private final Condition notEmpty;
	/** Condition for waiting puts */
	private final Condition notFull;

	/** 队列中的元素的byte[]数据的平均长度 */
	private int expectObjectSize = 255;// put数据时可能会改变值
	/** 队列扩容时的预期 */
	private final int expectNum;
	/** 是否初始化完成 */
	private final boolean init;

	/**
	 * 
	 * @param maxSize
	 * @param queuePath
	 */
	private PersistenceBlockingQueue(int maxSize, String queuePath) {
		this.maxSize = maxSize;
		this.queuePath = queuePath;
		this.lock = new ReentrantLock();
		this.notEmpty = lock.newCondition();
		this.notFull = lock.newCondition();
		this.expectNum = 16;
		init = initQueue(size);
		if (!init)
		{
			throw new RuntimeException("can't create QueueBuffer");
		}
	}

	/**
	 * 
	 * @param maxSzie
	 * @param expectNum
	 * @param queuePath
	 */
	private PersistenceBlockingQueue(int maxSzie, int expectNum, String queuePath) {
		this.maxSize = maxSzie;
		this.queuePath = queuePath;
		this.lock = new ReentrantLock();
		this.notEmpty = lock.newCondition();
		this.notFull = lock.newCondition();
		this.expectNum = expectNum;
		init = initQueue(size);
		if (!init)
		{
			throw new RuntimeException("can't create QueueBuffer");
		}
	}

	
	/**
	 * 初始化队列
	 * 
	 * @param size
	 *            为0时为new,不为0时为resize
	 * @return
	 */
	private boolean initQueue(int size) {
		lock.lock();
		try
		{
			try
			{
				RandomAccessFile raf = new RandomAccessFile(queuePath, "rw");
				FileChannel channel = raf.getChannel();
				MappedByteBuffer queueInfoMapped = channel.map(
						MapMode.READ_WRITE, 0, QUEUE_INFO_LENGTH);
				IntBuffer info = queueInfoMapped.asIntBuffer();
				MappedByteBuffer queueBuffer = null;
				if (size == 0)// new
				{
					head = info.get(0);
					tail = info.get(1);
					count = info.get(2);
					this.size = info.get(3);
					queueBuffer = channel.map(MapMode.READ_WRITE, QUEUE_INFO_LENGTH, this.size);
				}
				else// resize
				{
					queueBuffer = channel.map(MapMode.READ_WRITE, QUEUE_INFO_LENGTH, size);
				}
				this.queueInfoMapped = queueInfoMapped;
				this.queueInfoBuffer = info;
				this.queueBuffer = queueBuffer;
				return true;
			} catch (Exception e) {e.printStackTrace();}
			
		} finally {
			lock.unlock();
		}
		return false;
	}

	
	/**
	 * 
	 */
	@Override
	public E poll() {
		final Lock lock = this.lock;
		lock.lock();
		try
		{
			return count==0 ? null : dequeue() ;
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final Lock lock = this.lock;
		lock.lockInterruptibly();
		try {
			for (;;) {
				if (count != 0)
				{
					return dequeue();
				}
				
				if (nanos <= 0)
				{
					return null;
				}
				
				try
				{
					nanos = notEmpty.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					notEmpty.signal(); // propagate to non-interrupted thread
					throw ie;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 * @return
	 */
	private E dequeue() {
		E e = extract();
		if (e != null) {
			writeHead();
			decCount();
			// signal producter
			notFull.signal();
		}
		return e;
	}

	
	/**
	 * 
	 * @return
	 */
	private E extract() {
		queueBuffer.position(head);
		byte[] data = readBytes(queueBuffer);
		try
		{
			return convertBytes2Element(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * 
	 */
	@Override
	public E peek() {
		final Lock lock = this.lock;
		lock.lock();
		try {
			if (count == 0) return null;
			E e = extract();
			// 因为extract()中读取数据时调整了buffer位置，peek应该将位置调整回来
			queueBuffer.position(head);
			return e;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Inserts the specified element at the tail of this queue if it is possible
	 * to do so immediately without exceeding the queue's capacity, returning
	 * <tt>true</tt> upon success and <tt>false</tt> if this queue is full. This
	 * method is generally preferable to method {@link #add}, which can fail to
	 * insert an element only by throwing an exception.
	 * 
	 * @throws NullPointerException
	 *             if the specified element is null
	 * @throws RuntimeException
	 *             if the specified element not implements java.io.Serializable
	 */
	@Override
	public boolean offer(E e) {
		checkElement(e);
		byte[] data = convertElement2Bytes(e);
		if (data == null) {
			return false;
		}
		final Lock lock = this.lock;
		lock.lock();
		try {
			// if can enqueue, en queue
			return canEnqueue(data) ? enqueue(data) : false;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * is the specified element can enqueue
	 * 
	 * @param data
	 * @return
	 */
	private boolean canEnqueue(byte[] data) {
		if (data == null) return false;
		// ---->head---->tail---->
		// -----------data------------
		// ---->tail---->head---->
		// ----------capacity-----
		int left = calLeftCapacity();
		int len = data.length;
		// 再考虑下 期望值是否在这里计算
		expectObjectSize = expectObjectSize == len || expectObjectSize == 0 ? len : (len + expectObjectSize) / 2;
		// 空间不足则扩容
		return len > left ? resize() : true;
	}

	/**
	 * calculate left capacity
	 * 
	 * @return
	 */
	private int calLeftCapacity() {
		if (count == 0) {
			return size;
		}

		if (tail < head) {
			return head - tail;
		} else if (tail > head) {
			return head + (size - tail);
		} else {
			return 0;
		}
	}

	/**
	 * Inserts the specified element at the tail of this queue, waiting up to
	 * the specified wait time for space to become available if the queue is
	 * full.
	 * 
	 * @throws InterruptedException
	 *             {@inheritDoc}
	 * @throws NullPointerException
	 *             {@inheritDoc}
	 * @throws RuntimeException
	 *             {@inheritDoc}
	 */
	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
		checkElement(e);
		long nanos = unit.toNanos(timeout);
		final Lock lock = this.lock;
		byte[] data = convertElement2Bytes(e);
		lock.lockInterruptibly();
		try {
			boolean ok = true;
			for (;;) {
				// 如果ok,则入队列
				ok = canEnqueue(data) ? enqueue(data) : false;
				
				if (ok) return ok;
				
				if (nanos <= 0) return false;
				
				try 
				{
					nanos = notFull.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					notFull.signal(); // propagate to non-interrupted thread
					throw ie;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	private void writeHead() {
		this.head = queueBuffer.position();
		queueInfoBuffer.put(0, head);
	}

	
	/**
	 * 
	 */
	private void writeTail() {
		this.tail = queueBuffer.position();
		queueInfoBuffer.put(1, tail);
	}

	
	/**
	 * 
	 */
	private void addCount() {
		queueInfoBuffer.put(2, ++count);
	}

	
	/**
	 * 
	 */
	private void decCount() {
		queueInfoBuffer.put(2, --count);
	}

	
	/**
	 * 
	 * @param data
	 * @return
	 */
	private boolean enqueue(byte[] data) {
		int length = data.length;
		queueBuffer.position(tail);
		int t = size - tail;
		if (length > t) {
			queueBuffer.put(data, 0, t);
			queueBuffer.position(0);
			queueBuffer.put(data, t, length - t);
		} else {
			queueBuffer.put(data);
		}
		// signal consumer
		notEmpty.signal();
		writeTail();
		addCount();
		return true;
	}

	
	/**
	 * 扩容,与原先的区别，扩容成功后只存储有效数据。
	 * 
	 * @return
	 */
	private boolean resize() {
		int newsize = size + expectObjectSize * expectNum;
		newsize = newsize > maxSize ? maxSize : newsize;

		// copy old data
		byte[] oldData = null;
		if (count == 0) {
			// nothing
		} else if (tail > head) {
			oldData = new byte[tail - head];
			queueBuffer.position(head);
			queueBuffer.get(oldData, 0, tail - head);
		} else {
			// count>0 && tail==head ===>full
			// or tail<head ===>size-head+tail
			int len = tail == head ? size : size - head + tail;
			oldData = new byte[len];
			queueBuffer.position(head);
			queueBuffer.get(oldData, 0, size - head);
			queueBuffer.position(0);
			queueBuffer.get(oldData, size - head, tail);
		}
		// init = clear & creat
		boolean success = initQueue(newsize);
		if (success) {
			size = newsize;
			head = tail = 0;// count==0
			if (oldData != null) {
				tail = oldData.length;
				queueBuffer.put(oldData, 0, tail);
			}
			queueInfoBuffer.put(0, head);
			queueInfoBuffer.put(1, tail);
			queueInfoBuffer.put(2, count);
			queueInfoBuffer.put(3, size);
			return true;
		}
		return false;
	}

	
	/**
	 * 
	 */
	@Override
	public void put(E e) throws InterruptedException {
		checkElement(e);
		byte[] data = convertElement2Bytes(e);
		final Lock lock = this.lock;
		lock.lockInterruptibly();
		try
		{
			try
			{
				while (!canEnqueue(data)) notFull.await();
			}
			catch (InterruptedException ie)
			{
				notFull.signal(); // propagate to non-interrupted thread
				throw ie;
			}
			enqueue(data);
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public E take() throws InterruptedException {
		final Lock lock = this.lock;
		lock.lockInterruptibly();
		try
		{
			try
			{
				while (count == 0)
				{
					notEmpty.await();
				}
			}
			catch (InterruptedException ie)
			{
				notEmpty.signal(); // propagate to non-interrupted thread
				throw ie;
			}
			return dequeue();
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public int remainingCapacity() {
		final Lock lock = this.lock;
		lock.lock();
		try {
			return calLeftCapacity();
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public int drainTo(Collection<? super E> c) {
		if (c == null) throw new NullPointerException();
		if (c == this) throw new IllegalArgumentException();
		final Lock lock = this.lock;
		lock.lock();
		try
		{
			int n = 0;
			queueBuffer.position(head);
			while (n < count) {
				byte[] data = readBytes(queueBuffer);
				E e = this.convertBytes2Element(data);
				if (e != null)
				{
					c.add(e);
					n++;
				}
			}
			
			if (n > 0)
			{
				count = 0;
				queueInfoBuffer.put(2, count);
				writeHead();
				notFull.signalAll();
			}
			return n;
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		if (c == null) throw new NullPointerException();
		if (c == this) throw new IllegalArgumentException();
		if (maxElements <= 0) return 0;
		final Lock lock = this.lock;
		lock.lock();
		try
		{
			int n = 0;
			int max = (maxElements < count) ? maxElements : count;
			queueBuffer.position(head);
			while (n < max) {
				byte[] data = readBytes(queueBuffer);
				E e = this.convertBytes2Element(data);
				if (e != null) {
					c.add(e);
					n++;
				}
			}
			if (n > 0) {
				count -= n;
				queueInfoBuffer.put(2, count);
				writeHead();
				notFull.signalAll();
			}
			return n;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) return false;
		final Lock lock = this.lock;
		lock.lock();
		final MappedByteBuffer mb = queueBuffer;
		int hp = mb.position();// hp = head postion
		try 
		{
			int c = count;
			if (c == 0) return false;
			
			mb.position(head);
			int n = 0;
			while (n < c) {
				byte[] data = readBytes(mb);
				E e = convertBytes2Element(data);
				if (e == null) return false;
				
				if (o.equals(e)) return true;
				
				n++;
			}
			return false;
		} finally {
			mb.position(hp);
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Deprecated
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("can't use remove()!!!");
	}

	
	/**
	 * 这个方法没法实现,因为在并发环境下没办法准确计算下一个元素的位置 调用时会抛出RuntimeException
	 */
	@Deprecated
	@Override
	public Iterator<E> iterator() {
		throw new UnsupportedOperationException("can't use iterator()!!!");
	}

	
	/**
	 * 
	 */
	@Override
	public Object[] toArray() {
		int c = count;
		if (c == 0) return new Object[0];
		
		final Lock lock = this.lock;
		lock.lock();
		final MappedByteBuffer mb = queueBuffer; int np = mb.position();// np = now postion
		try 
		{
			mb.position(head) ;
			Object[] a = new Object[c];
			int k = 0;
			while (k < c)
			{
				byte[] data = readBytes(mb);
				a[k++] = convertBytes2Element(data);
			}
			return a;
		} finally {
			mb.position(np);
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		int c = count;
		if (c == 0) return a;

		final Lock lock = this.lock;
		lock.lock();
		final MappedByteBuffer mb = queueBuffer; int np = mb.position();
		try
		{
			mb.position(head) ;
			if (a.length < count) a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), c);
			int k = 0;
			while (k < c)
			{
				byte[] data = readBytes(mb);
				a[k++] = (T) convertBytes2Element(data);
			}
			
			while (a.length > c)
			{
				a[c++] = null;
			}
			return a;
		} finally {
			mb.position(np);
			lock.unlock();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public int size() {
		return count;
	}

	
	/**
	 * 
	 * @param e
	 */
	protected void checkElement(E e) {
		if (e == null) throw new NullPointerException();
		if (!(e instanceof Serializable)) throw new IllegalArgumentException("e must implements java.io.Serializable");
	}

	
	/**
	 * convert the specified element to byte[]
	 * 
	 * @param e
	 * @return
	 */
	protected byte[] convertElement2Bytes(E e) {
		try {
			ByteArrayOutputStream eBytesArray = new ByteArrayOutputStream();
			ObjectOutputStream eOutput = new ObjectOutputStream(eBytesArray);
			eOutput.writeObject(e);
			return encodeBytes(eBytesArray.toByteArray());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
	/**
	 * convert byte[] to the specified element
	 * 
	 * @param data
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected E convertBytes2Element(byte[] data) {
		if (data == null || data.length == 0) return null;
		try
		{
			return (E) new ObjectInputStream(new ByteArrayInputStream(data)) .readObject();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	
	/**
	 * 转化data数组.转换为data长度+内容并返回
	 * 
	 * @param data
	 *            要转化的byte[]
	 * @return 转换为data长度+内容的byte[]
	 */
	protected byte[] encodeBytes(byte[] data) {
		// null或者空串,直接用0表示
		if (data == null || data.length == 0) return new byte[] {0};

		// chg by wuqq.对象的创建下移
		byte[] lenbytes = encodeUnsignInt(data.length);
		ByteArrayOutputStream bos = new ByteArrayOutputStream(lenbytes.length + data.length);
		try {
			// 写len
			bos.write(lenbytes);
			// 写内容
			bos.write(data);
		} catch (IOException e) {// 不可能到这里
			throw new RuntimeException(e.getMessage());
		}
		return bos.toByteArray();
	}

	
	/**
	 * 
	 * @param value
	 * @return
	 */
	protected byte[] encodeUnsignInt(int value) {// 前2bit表示要用几个byte存储,只能存非负数,存储最大值1,073,741,824
		int len = 0;
		if (value < 0) {
			throw new RuntimeException("Value must > 0.");
		} else if (value <= 63) {// 用1字节存
			len = 1;
		} else if (value <= 16383) {// 用2字节存
			len = 2;
		} else if (value <= 4194303) {// 用3字节存
			len = 3;
		} else if (value <= 1073741823) {// 用4字节存
			len = 4;
		} else {// 超出可存储最大值
			throw new RuntimeException("Value too large!");
		}

		byte[] data = new byte[len];
		for (int i = 0; i < len; i++) {
			data[len - i - 1] = (byte) (value >> 8 * i & 0xFF);
		}
		data[0] |= (len - 1) << 6;
		return data;
	}

	/**
	 * 
	 * @param buf
	 * @return
	 */
	protected byte[] readBytes(ByteBuffer buf) {
		int len = readUnsignInt(buf);
		return readBytes(buf, len) ;
	}
	
	/**
	 * 
	 * @param buf
	 * @param len
	 * @return
	 */
	protected byte[] readBytes(ByteBuffer buf, int len) {
		byte[] data = new byte[len];
		if (buf.remaining() < len) {
			int remain = buf.remaining();
			buf.get(data, 0, remain);
			buf.position(0);
			buf.get(data, remain, len - remain);
		} else {
			buf.get(data);
		}
		return data ;
	}

	
	/**
	 * 
	 * @param buf
	 * @return
	 */
	protected int readUnsignInt(ByteBuffer buf) {
		int result = 0;
		byte b = getOneByte(buf);
		int len = (b & 0xC0) >>> 6;
		b &= 0x3F;
		for (int i = len;; i--) {
			result += (b & 0xFF) << (8 * i);

			if (i == 0)
				break;
			else
				b = getOneByte(buf);
		}
		return result;
	}

	/**
	 * 
	 * @param buf
	 * @return
	 */
	protected byte getOneByte(ByteBuffer buf) {
		if (!buf.hasRemaining()) {
			buf.position(0);
		}
		return buf.get();
	}

	
	/**
	 * 将此缓冲区所做的内容更改强制写入queuePath对应的映射文件中. 增加了@PreDestroy注解标签,JVM正常退出时会调用该方法
	 */
	@PreDestroy
	public void force() {
		lock.lock();
		try {
			if (queueInfoMapped != null) {
				queueInfoMapped.force();
			}
			if (queueBuffer != null) {
				queueBuffer.force();
			}
		} finally {
			lock.unlock();
		}
	}
}
