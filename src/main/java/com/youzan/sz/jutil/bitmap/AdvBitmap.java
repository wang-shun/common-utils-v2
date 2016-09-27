package com.youzan.sz.jutil.bitmap;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Tools;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
//
//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Tools;

/**
 * 新版本支持21亿的uin的bitmap
 * XXX 不过只支持每个key存一个bit
 * @author sunnyin
 * @create 2011-1-5
 */
public class AdvBitmap implements Bitmap
{
	private static       Logger logger       = Logger.getLogger("bitmap");
	// 最大只支持2G的mmap文件 (Integer.MAX_VALUE - MAX_EXT_SIZE - 1)*8L
	private static final long   MAX_CAPACITY = 17179869104L;
	private static final int    LOCKS        = 13;
	// bitmap的存储文件名称
	private String bitmapFileName;
	// mmap中的数据块,只预留空间
	private ByteBuffer buff;
	// 数据块中的头信息，具体存什么内容由业务决定.这里只预留空间
	private ByteBuffer extInfo;
	// 容量
	private long capacity;
	private int extSize;
	private long maxSize;
	private ReentrantReadWriteLock[] locks = new ReentrantReadWriteLock[LOCKS];
	private Lock extLock = new ReentrantLock();

	public AdvBitmap(String name, String path, Properties prop)
	{
		capacity = StringUtil.convertLong(prop.getProperty("capacity"), 0);
		if (capacity <= 0)
			throw new RuntimeException("capactiy <=0 " + capacity);
		if (capacity > MAX_CAPACITY)
			throw new RuntimeException("capactiy must less than " + MAX_CAPACITY);
		for(int i = 0;i < locks.length;i++)
		{
			locks[i] = new ReentrantReadWriteLock();
		}
		// XXX extSize默认为4
		extSize = StringUtil.convertInt(prop.getProperty("extSize"), 4);
		if (extSize > MAX_EXT_SIZE)
			throw new RuntimeException("extSize must less than " + MAX_EXT_SIZE);
		// extSize最大只支持到8B
		this.extSize = Math.min(extSize, MAX_EXT_SIZE);
		this.bitmapFileName = path + "/" + name + ".mmap";
		init();
	}

	/**
	 * 初始化操作，创建mmap等
	 */
	private void init()
	{
		try
		{
			File file = new File(bitmapFileName);
			RandomAccessFile storeFile = new RandomAccessFile(file, "rw");
			FileChannel channel = storeFile.getChannel();
			long totalSize = capacity;
			// TODO 测试maxSize是否超过Integer.MAX
			maxSize = extSize + totalSize / 8 + 1;
			System.out.println("maxSize:" + maxSize + "|" + Integer.MAX_VALUE);
			buff = Tools.safetyMapping(channel, MapMode.READ_WRITE, 0, maxSize);
			ByteBuffer tmpBuffer = buff.duplicate();
			tmpBuffer.limit(extSize);
			extInfo = tmpBuffer.slice();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.fatal("AdvBitmap init error!", e);
		}
	}

	@Override
	public long capacity()
	{
		return capacity;
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("clear not support!");
	}

	@Override
	public byte getBit(int uin)
	{
		throw new UnsupportedOperationException("value not support more 1 bit!");
	}

	@Override
	public boolean getBit(long uin)
	{
		if (uin < 0 || uin >= capacity)
			throw new IllegalArgumentException("uin < 0");
		Lock lock = locks[Math.abs((int) uin) % LOCKS].readLock();
		lock.lock();
		try
		{
			int index = extSize + (int) (uin / 8);
			int offset = (int) (uin % 8);
			int mask = 1 << offset;
			return (buff.get(index) & mask) > 0;
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public byte[] getExtInfo()
	{
		extLock.lock();
		try
		{
			ByteBuffer tmpBuffer = extInfo.duplicate();
			byte[] dst = new byte[extSize];
			tmpBuffer.get(dst);
			return dst;
		}
		finally
		{
			extLock.unlock();
		}
	}

	@Override
	public void setBit(int uin, byte value)
	{
		throw new UnsupportedOperationException("value not support more 1 bit!");
	}

	@Override
	public void setBit(long uin, boolean value)
	{
		if (uin < 0 || uin >= capacity)
			throw new IllegalArgumentException("uin < 0");
		Lock lock = locks[Math.abs((int) uin) % LOCKS].writeLock();
		lock.lock();
		try
		{
			int index = extSize + (int) (uin / 8);
			int offset = (int) (uin % 8);
			int mask = (1 << offset);
			if (value)
			{
				byte byteValue = buff.get(index);
				byteValue |= mask;
				buff.put(index, byteValue);
			}
			else
			{
				byte byteValue = buff.get(index);
				byteValue &= ~mask;// 设置某位为0
				buff.put(index, byteValue);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void setExtInfo(byte[] data)
	{
		if (data == null || data.length <= 0)
			throw new NullPointerException("parm data is null");
		byte[] tmp = new byte[extSize];
		int length = Math.min(data.length, extSize);
		System.arraycopy(data, 0, tmp, 0, length);
		extLock.lock();
		try
		{
			ByteBuffer tmpBuffer = extInfo.duplicate();
			tmpBuffer.put(tmp);
		}
		finally
		{
			extLock.unlock();
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("path:").append(bitmapFileName).append("\n");
		sb.append("capacity:").append(capacity).append("\n");
		sb.append("extSize:").append(extSize).append("\n");
		return sb.toString();
	}

	public static void main(String[] args)
	{
		// long capacity = 17179869L;
		// AdvBitmap bitmap = new AdvBitmap(capacity,4,"bmp",
		// "E://var//bitmap");
		// Random random = new Random();
		// System.out.println("start!");
		// for(int i = 0;i < 100000;i++)
		// {
		// long idx = random.nextLong();
		// boolean value = random.nextBoolean();
		// bitmap.setBit(idx, value);
		// if(value != bitmap.getBit(idx))
		// System.err.println(value +"!="+bitmap.getBit(idx));
		// }
		// System.out.println("end!");
	}

}
