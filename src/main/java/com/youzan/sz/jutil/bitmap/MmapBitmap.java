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
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Tools;

/**
 * 通过mmap存储bitmap的数据
 * @author sunnyin
 * @create 2009-9-14
 */
public class MmapBitmap implements Bitmap
{
	private static       Logger logger  = Logger.getLogger("bitmap");
	//对应二进制数:1,11,111,1111,11111,111111,1111111,11111111
	private static       int[]  BIT_ARY =
	{ 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff };
	private static final int    LOCKS   = 10;
	//bitmap的存储文件名称
	private String bitmapFileName;
	//mmap中的数据块,只预留空间
	private ByteBuffer buff;
	//数据块中的头信息，具体存什么内容由业务决定.这里只预留空间
	private ByteBuffer extInfo;
	//容量
	private int capacity;
	//一个uin占用的bit位长度，通过配置文件
	private int bitLen;
	private int extSize;
	private int maxSize;
	private Lock[] locks = new ReentrantLock[LOCKS];
	private Lock extLock = new ReentrantLock();
	
	public MmapBitmap(String name, String path,Properties prop)
	{
		this(StringUtil.convertInt(prop.getProperty("capacity"), Integer.MAX_VALUE), StringUtil.convertInt(prop
				.getProperty("bitLen"), 1), StringUtil.convertInt(prop.getProperty("extSize"), 4), name, path);
	}

	public MmapBitmap(int capacity, int bitLen, int extSize, String name, String path)
	{
		this.capacity = Math.min(capacity, Integer.MAX_VALUE);
		//bitLen最大只支持到8B
		if(bitLen > MAX_BIT_LEN)
			throw new RuntimeException("bitLen must less than "+MAX_BIT_LEN);
		this.bitLen = Math.min(bitLen, MAX_BIT_LEN);
		if(extSize > MAX_EXT_SIZE)
			throw new RuntimeException("extSize must less than "+MAX_EXT_SIZE);
		//extSize最大只支持到8B
		this.extSize = Math.min(extSize,  MAX_EXT_SIZE);
		this.bitmapFileName = path + "/" + name + ".mmap";
		init();
	}

	/**
	 * 初始化操作，创建mmap等
	 */
	private void init()
	{
		for (int i = 0; i < locks.length; i++)
		{
			locks[i] = new ReentrantLock();
		}
		try
		{
			File file = new File(bitmapFileName);
			RandomAccessFile storeFile = new RandomAccessFile(file, "rw");
			FileChannel channel = storeFile.getChannel();
			long totalSize = 1L * capacity * bitLen;
			maxSize = extSize + (int) (totalSize / 8) + 1;
			buff = Tools.safetyMapping(channel, MapMode.READ_WRITE, 0, maxSize);
			ByteBuffer tmpBuffer = buff.duplicate();
			tmpBuffer.limit(extSize);
			extInfo = tmpBuffer.slice();
		} catch (Exception e)
		{
			e.printStackTrace();
			logger.fatal("MmapBuffer init error!", e);
		}
	}

	public int total()
	{
		int sum = 0;
		for (int i = 0; i < capacity; i++)
		{
			if (getBit(i) > 0)
				sum++;
		}
		return sum;
	}

	/**
	 * 获取指定uin所对应的bit位数据
	 * @param uin
	 * @return 
	 */
	public byte getBit(int uin)
	{
		if (uin < 0 || uin >= capacity)
			throw new IllegalArgumentException("uin < 0");
		//如果uin过大，会超过Int.Max的上限
		long beginPos = 1L * (uin + 1) * bitLen - 1;
		long endPos = 1L * uin * bitLen;
		long separator = beginPos / 8 * 8;
		if (beginPos / 8 == endPos / 8)
		{
			//start与end在同一个byte中		
			int index = extSize + (int) (beginPos / 8);
			int start = (int) (beginPos - separator);
			int end = (int) (endPos - separator);
			return getValue(start, end, buff.get(index));
		} else
		{
			//分界点
			int mov = (int) (separator - endPos);
			//start与end在不同的byte中，则需要分别获取高位和地位的byte值，然后再组合
			int index = extSize + (int) (beginPos / 8);
			int start = (int) (beginPos - separator);
			byte high = getValue(start, 0, buff.get(index));
			index = extSize + (int) (endPos / 8);
			int end = 8 - (int) (separator - endPos);
			byte low = getValue(7, end, buff.get(index));
			return (byte) (high << mov | low);
		}
	}
	
	@Override
	public boolean getBit(long uin)
	{
		throw new UnsupportedOperationException("old value not support long key!");
	}

	/**
	 * 设置指定uin的bit位数据
	 * @param uin
	 * @param value 当做无符号数进行处理
	 */
	public void setBit(int uin, byte value)
	{
		if (uin < 0 || uin >= capacity)
			throw new IllegalArgumentException("uin < 0");
		//value的值域判断
		if ((value & 0xff) > BIT_ARY[bitLen - 1])
			throw new IllegalArgumentException("value out of bounds");
		long beginPos = 1L * (uin + 1) * bitLen - 1;
		long endPos = 1L * uin * bitLen;
		long separator = beginPos / 8 * 8;
		if (beginPos / 8 == endPos / 8)
		{
			//start与end在同一个byte中	
			int index = extSize + (int) (beginPos / 8);
			//按index来取锁
			locks[index % 10].lock();
			try
			{
				byte byteOp = buff.get(index);
				int start = (int) (beginPos - separator);
				int end = (int) (endPos - separator);
				byteOp = setValue(start, end, value, byteOp);
				buff.put(index, byteOp);
			} finally
			{
				locks[index % 10].unlock();
			}
		} else
		{
			//分界点
			int mov = (int) (separator - endPos);
			//高位
			int index = extSize + (int) (beginPos / 8);
			byte high = (byte) ((value >>> mov) & 0xff);
			locks[index % 10].lock();
			byte byteOp;
			try
			{
				byteOp = buff.get(index);
				int start = (int) (beginPos - separator);
				byteOp = setValue(start, 0, high, byteOp);
				buff.put(index, byteOp);
			} finally
			{
				locks[index % 10].unlock();
			}

			//低位
			index = extSize + (int) (endPos / 8);
			byte low = (byte) (value & BIT_ARY[mov - 1]);
			locks[index % 10].lock();
			try
			{
				byteOp = buff.get(index);
				int end = 8 - (int) (separator - endPos);
				byteOp = setValue(7, end, low, byteOp);
				buff.put(index, byteOp);
			} finally
			{
				locks[index % 10].unlock();
			}
		}
	}
	
	@Override
	public void setBit(long uin, boolean value)
	{
		throw new UnsupportedOperationException("old value not support long key!");
	}

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
		} finally
		{
			extLock.unlock();
		}
	}

	public byte[] getExtInfo()
	{
		extLock.lock();
		try
		{
			ByteBuffer tmpBuffer = extInfo.duplicate();
			byte[] dst = new byte[extSize];
			tmpBuffer.get(dst);
			return dst;
		} finally
		{
			extLock.unlock();
		}
	}

	/**
	 * 获取byteVal中从start到end个bit位对应的值
	 * @param start
	 * @param end
	 * @param byteOp
	 * @return 
	 */
	private byte getValue(int start, int end, byte byteOp)
	{
		if (start < 0 || start >= MAX_BIT_LEN)
			throw new IllegalArgumentException("parm start must >=0&&<8");
		if (end < 0 || end >= MAX_BIT_LEN)
			throw new IllegalArgumentException("parm end must >=0&&<8");
		if (start < end)
			throw new IndexOutOfBoundsException("parm start must >= parm end");
		return (byte) ((byteOp >>> end) & BIT_ARY[start - end]);
	}

	/**
	 * 将由start和end指定的bit位设置为byteVal,byteOp其他位不变
	 * @param start
	 * @param end
	 * @param byteVal设置的value
	 * @param byteOp待设置的byte值
	 * @return 设置后的byteOp
	 */
	private byte setValue(int start, int end, byte byteVal, byte byteOp)
	{
		if (start < 0 || start >= MAX_BIT_LEN)
			throw new IllegalArgumentException("parm start must >=0&&<8");
		if (end < 0 || end >= MAX_BIT_LEN)
			throw new IllegalArgumentException("parm end must >=0&&<8");
		if (start < end)
			throw new IndexOutOfBoundsException("parm start must >= parm end");
		if ((byteVal & 0xff) > BIT_ARY[start - end])
			throw new IllegalArgumentException("parm byteVal must <= " + BIT_ARY[start - end]);
		return (byte) ((byteOp & ~(BIT_ARY[start - end] << end)) | (byteVal << end));
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("path:").append(bitmapFileName).append("\n");
		sb.append("capacity:").append(capacity).append("\n");
		sb.append("bitLen:").append(bitLen).append("\n");
		sb.append("extSize:").append(extSize).append("\n");
		return sb.toString();
	}

	/*全部置位此bitmap*/
	public void clear()
	{
		int blockSize = 1024 * 1024;
		int blockCount = (int) (this.maxSize / (blockSize));
		int residual = maxSize % blockSize;
		if (blockCount == 0) //<1024*1024
		{
			blockSize = maxSize;
		}
		byte[] emptyBlock = new byte[blockSize];
		if (emptyBlock == null)
		{
			throw new RuntimeException("error alloc empty block");
		}
		int endIndex = maxSize - residual;
		int i = 0;
		for (i = 0; i < endIndex && buff.hasRemaining(); i += blockSize)
		{
			buff.put(emptyBlock, 0, blockSize);
		}
		if (residual > 0)
		{
			emptyBlock = new byte[residual];
			buff.put(emptyBlock, 0, residual);
		}
	}
	
	@Override
	public long capacity()
	{
		return capacity;
	}

	public static void main(String[] arg)
	{
		int capacity = 100000;
		int bitLen = 9;
		MmapBitmap bitmap = new MmapBitmap(capacity,bitLen,4,"bmp", "E://var//bitmap");
		Random random = new Random();
		System.out.println("start!");
		for(int i = 0;i < 100000;i++)
		{
			int idx = random.nextInt(capacity);
			int value = random.nextInt(bitLen);
			bitmap.setBit(idx, (byte)value);
//			System.out.println(value+"|"+bitmap.getBit(idx));
			if(value != bitmap.getBit(idx))
				System.err.println(value +"!="+bitmap.getBit(idx));
		}
		System.out.println("end!");
	}
}
