package com.youzan.sz.jutil.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//import com.qq.jutil.bytes.ByteUtil;
//import com.qq.jutil.bytes.Bytesable;
//import com.qq.jutil.bytes.Debyter;


/**
 * 支持位数据读写的数据结构
 * @author stonexie
 *
 */
@SuppressWarnings("serial")
public class BitBuffer implements Bytesable
{
	private int capacity;//存储容量
	private byte[] data;//数据
	
	public BitBuffer()
	{	
	}
	/**
	 * @param capacity 存储容量
	 */
	public BitBuffer(int capacity)
	{
		this.capacity = capacity;
		this.data = new byte[(capacity + 8 - 1) / 8];
	}
	
	/**
	 * @param data
	 */
	public BitBuffer(byte[] data)
	{
		this(data, data.length * 8);
	}
	
	/**
	 * @param data
	 * @param capacity
	 */
	public BitBuffer(byte[] data, int capacity)
	{
		if(capacity > data.length * 8)
		{
			throw new RuntimeException("Capacity too large,byte[] can not store.");
		}
		this.data = data;
		this.capacity = capacity;
	}
	
	/**
	 * 从offset开始,顺序将buffer的位数据写入bitbuffer中
	 * @param offset
	 * @param buffer
	 */
	public void put(int offset, BitBuffer buffer)
	{
		for (int i = 0; i < buffer.getCapacity(); i++)
		{
			putBit(i + offset, buffer.getBit(i));
		}
	}
	
	/**
	 * 截取从offset后面length的位数据
	 * @param offset
	 * @param length
	 * @return BitBuffer
	 */
	public BitBuffer get(int offset, int length)
	{
		if(offset + length > this.capacity)
		{
			throw new RuntimeException("Index out of bound!");
		}
		BitBuffer buffer = new BitBuffer(length);
		for (int i = 0; i < length; i++)
		{
			buffer.putBit(i, getBit(offset + i));
		}
		return buffer;
	}
	
	/**
	 * 设置某一位
	 * @param pos
	 * @param value
	 */
	public void putBit(int pos, boolean value)
	{
		int idx = pos / 8;
		if(idx >= data.length)
		{
			throw new RuntimeException("Out of range!pos:"+ pos +"\tdata length:"+ data.length);
		}
		int mask = (1 << (pos % 8));
		if(value)
		{
			data[idx] |= mask;//设置某位为1
		}
		else
		{
			data[idx] &= ~mask;//设置某位为0
		}
	}

	/**
	 * 读取某一位
	 * @param pos
	 * @return boolean
	 */
	public boolean getBit(int pos)
	{
		int idx = pos / 8;
		if(idx >= data.length)
		{
			throw new RuntimeException("Out of range!");
		}
		int mask = (1 << (pos % 8));
		return (data[idx] & mask) > 0;
	}
	
	/**
	 * 读某一位
	 * @param pos
	 * @return boolean
	 */
	public boolean getBoolean(int pos)
	{
		return getBit(pos);
	}
	
	/**
	 * 设置某一位
	 * @param pos
	 * @param value
	 */
	public void putBoolean(int pos, boolean value)
	{
		putBit(pos, value);
	}
	
	
	/**
	 * 读取从offset开始后面length位,以int形式返回
	 * @param offset
	 * @param length
	 * @return int
	 */
	public int getInt(int offset, int length)
	{
		if(length > 32)
		{
			throw new RuntimeException("Int length can not large than 32 bit! length:"+ length);
		}
		int result = 0;
		for (int i = 0; i < length; i++)
		{
			result <<= 1;
			result += getBit(offset + length - i - 1) ? 1 : 0;
		}
		return result;
	}
	
	/**
	 * 设置int值
	 * @param offset
	 * @param length
	 * @param value
	 */
	public void putInt(int offset, int length, int value)
	{
		if(length > 32)
		{
			throw new RuntimeException("Type int length can not large than 32 bit! length:"+ length);
		}
		int mask = 1;
		for (int i = 0; i < 32; i++)
		{
			boolean flag = (value & mask) > 0;
			if(flag)
			{
				if(i >= length)
				{
					throw new RuntimeException("Value too large,can not store!");
				}
				putBit(i + offset, true);
			}
			else
			{
				if(i < length)
				{
					putBit(i + offset, false);
				}
			}
			value >>>= 1;
		}
	}
	
	/**
	 * 读取long值
	 * @param offset
	 * @param length
	 * @return long
	 */
	public long getLong(int offset, int length)
	{
		if(length > 64)
		{
			throw new RuntimeException("Type long length can not large than 64 bit! length:"+ length);
		}
		long result = 0;
		for (int i = 0; i < length; i++)
		{
			result <<= 1;
			result += getBit(offset + length - i - 1) ? 1 : 0;
		}
		return result;
	}
	
	/**
	 * 设置long值
	 * @param offset
	 * @param length
	 * @param value
	 */
	public void putLong(int offset, int length, long value)
	{
		if(length > 64)
		{
			throw new RuntimeException("Type long length can not large than 64 bit! length:"+ length);
		}
		long mask = 1;
		for (int i = 0; i < 64; i++)
		{
			boolean flag = (value & mask) > 0;
			if(flag)
			{
				if(i >= length)
				{
					throw new RuntimeException("Value too large,can not store!");
				}
				putBit(i + offset, true);
			}
			else
			{
				if(i < length)
				{
					putBit(i + offset, false);
				}
			}
			value >>>= 1;
		}
	}

	
	/**
	 * 取得bitbuffer的容量
	 * @return int
	 */
	public int getCapacity()
	{
		return capacity;
	}
	
	/**
	 * bitbuffer转成byte数组形式
	 * @return byte[]
	 */
	public byte[] toArray()
	{
		return this.data;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[BitBuffer: "+ this.capacity +", ");
		for (int i = 0; i < this.capacity; i++)
		{
			sb.append(getBit(i) ? "1" : "0");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public BitBuffer initFromBytes(byte[] bs)
	{
		ByteBuffer bb = ByteBuffer.wrap(bs);
		int capacity = Debyter.getInt(bb);
		byte[] data = Debyter.getBytes(bb);
		return new BitBuffer(data, capacity);
	}

	public byte[] toBytes()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			bos.write(ByteUtil.enbyteInt(capacity));
			bos.write(ByteUtil.enbyteBytes(data));

		}
		catch(IOException e)
		{
			throw new RuntimeException("ToBytes fail."+ e.getMessage());
		}
		return bos.toByteArray();
	}

	public static void main(String[] args)
	{
		BitBuffer buffer = new BitBuffer(10);
		//buffer.putLong(1, 63, 100);
		System.out.println(buffer.initFromBytes(buffer.toBytes()));
	}

}
