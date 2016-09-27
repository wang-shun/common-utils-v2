package com.youzan.sz.jutil.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class PersistenQueue
{
	private IntBuffer info;//head|tail|count
	private MappedByteBuffer ququeBuffer;//
	
	private final ReentrantLock lock;
	private final Condition notEmpty;
	private int maxMemSize;
	private String path;
	
	/**
	 * 
	 * @param initMemSize 初始化时队列文件的大小
	 * @param maxMemSize 队列文件允许的最大size
	 * @param path 队列文件的存储目录
	 * @param name 队列文件的名字
	 */
	public PersistenQueue(int initMemSize, int maxMemSize, String path, String name)
	{
		this.maxMemSize = maxMemSize;
		this.lock = new ReentrantLock();
		this.notEmpty = this.lock.newCondition();
		this.path = path +"/"+ name +".q";
		initQueue(this.path, initMemSize);
	}
	
	/**
	 * 增加一个元素到队列中,如果插入成功则返回true，如果超出队列允许的最大大小则返回false
	 * @param item
	 * @return boolean
	 */
	public boolean offer(byte[] item)
	{
		lock.lock();
		try
		{
			int tail = tail();
			int head = head();
			int capacity = capacity();
			byte[] bs = encodeBytes(item);

			if(tail >= head)
			{
				//计算还欠多少内存
				int needSize = tail - head + bs.length - capacity;
				if(needSize > 0)
				{//空闲块不够了
					if(needSize + capacity() > maxMemSize)
					{
						return false;
					}
					else
					{
						extend(needSize);//扩展
						return offer(item);
					}
				}
				
				if(tail + bs.length <=  capacity)
				{
					this.ququeBuffer.position(tail);
					this.ququeBuffer.put(bs);
				}
				else
				{
					this.ququeBuffer.position(tail);
					int partOne = capacity() - tail;
					this.ququeBuffer.put(bs, 0, partOne);
					this.ququeBuffer.position(0);
					this.ququeBuffer.put(bs, partOne, bs.length - partOne);
				}
			}
			else
			{
				//计算还欠多少内存
				int needSize = bs.length - (head - tail);
				if(needSize > 0)
				{//空闲块不够了
					if(needSize + capacity > maxMemSize)
					{
						return false;
					}
					else
					{
						extend(needSize);//扩展
						return offer(item);
					}
				}
				
				if(head - tail < bs.length)
				{//空闲块不够了
					return false;
				}
				this.ququeBuffer.position(tail);
				this.ququeBuffer.put(bs);
			}
			setTail(ququeBuffer.position());
			countPlus(1);
			this.notEmpty.signal();
			//System.out.println("offer:"+ toString());
			return true;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * 从队头取出一个元素，如果队列中没有元素则阻塞等待
	 * @return byte[]
	 */
	public byte[] take()
	{
		lock.lock();
		try
		{
			while(count() == 0)
			{
				try
				{
					this.notEmpty.await();
				} 
				catch (InterruptedException e)
				{//这个异常不会被抛出
					e.printStackTrace();
				}
			}
			this.ququeBuffer.position(head());
			byte[] bs = readBytes(this.ququeBuffer);
			setHead(this.ququeBuffer.position());			
			countPlus(-1);
			//System.out.println("take:"+ toString());
			return bs;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	//非阻塞获取，队列中元素为空，则返回null
	public byte[] poll()
	{
		lock.lock();
		try
		{
			//size为0，则返回空
			if(count() == 0)
			{
				return null;
			}
			this.ququeBuffer.position(head());
			byte[] bs = readBytes(this.ququeBuffer);
			setHead(this.ququeBuffer.position());			
			countPlus(-1);
			//System.out.println("take:"+ toString());
			return bs;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	/**
	 * 从队头取出一个元素，如果没有元素直接返回null
	 * @return byte[]
	 */
	public byte[] peek()
	{
		lock.lock();
		try
		{
			if(count() == 0)
			{
				return null;
			}
			this.ququeBuffer.position(head());
			byte[] bs = readBytes(this.ququeBuffer);
			setHead(this.ququeBuffer.position());			
			countPlus(-1);
			//System.out.println("peek:"+toString());
			return bs;
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public int size()
	{
		return count();
	}
	
	public void clear()
	{
		lock.lock();
		try
		{
			this.info.put(0,0);
			this.info.put(1,0);
			this.info.put(2,0);
		}
		finally
		{
			lock.unlock();
		}
	}
	
	public String toString()
	{
		return "[PersistenQueue,head:"+ head() +"\ttail:"+ tail()+"\tcount:"+ count()+"]";
	}
	
////////////	
	private void extend(int needSize)
	{
		int capacity = capacity();
		int toSize = needSize <= capacity ? capacity * 2 : capacity + needSize;
		if(toSize > this.maxMemSize)
		{
			toSize = maxMemSize;
		}
		
		//System.out.println("extend:"+ toSize);
		//先把原来的数据导出来,扩展完后再put回去
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		byte[] bs = null;
		while((bs = peek()) != null)
		{
			list.add(bs);
		}
		File f = new File(path);
		f.delete();
		this.info = null;
		this.ququeBuffer = null;
		initQueue(path, toSize);
		setCapacity(toSize);
		for(byte[] item : list)
		{
			offer(item);
		}
	}	
	
	private void initQueue(String path, int mem)
	{
		try
		{
			RandomAccessFile raf = new RandomAccessFile(path, "rw");
			FileChannel channel = raf.getChannel();
			this.info = channel.map(MapMode.READ_WRITE, 0, 16).asIntBuffer();
			if(capacity() == 0)
			{
				this.ququeBuffer = channel.map(MapMode.READ_WRITE, 16, mem);
				setCapacity(mem);
			}
			else
			{//已经初始化过
				this.ququeBuffer = channel.map(MapMode.READ_WRITE, 16, capacity());
			}
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private int head()
	{
		return this.info.get(0);
	}
	private void setHead(int head)
	{
		this.info.put(0, head);
	}
	
	private int tail()
	{
		return this.info.get(1);
	}
	
	private void setTail(int tail)
	{
		this.info.put(1, tail);
	}
	
	private int count()
	{
		return this.info.get(2);
	}	
	
	private void countPlus(int i)
	{
		this.info.put(2, count() + i);
	}
	
	private int capacity()
	{
		return this.info.get(3);
	}
	
	private void setCapacity(int cap)
	{
		this.info.put(3, cap);
	}
/////////	
	private static byte[] encodeUnsignInt(int value)
	{//前2bit表示要用几个byte存储,只能存非负数,存储最大值1,073,741,824
		int len = 0;
		if(value < 0)
		{
			throw new RuntimeException("Value must > 0.");
		}
		else if(value <= 63)
		{//用1字节存
			len = 1;
		}
		else if(value <= 16383)
		{//用2字节存
			len = 2;
		}
		else if(value <= 4194303)
		{//用3字节存
			len = 3;
		}
		else if(value <= 1073741823)
		{//用4字节存
			len = 4;
		}
		else
		{//超出可存储最大值
			throw new RuntimeException("Value too large!");
		}
		
		byte[] data = new byte[len];
		for (int i = 0; i < len; i++)
		{
			data[len - i - 1] = (byte)(value >> 8 * i & 0xFF );
		}
		data[0] |= (len - 1) << 6;
		return data;
	}
	
	
	private static int readUnsignInt(ByteBuffer bf)
	{
		int result = 0;
		byte b = getOneByte(bf);
		int len = (b & 0xC0) >>> 6;
		b &= 0x3F; 
        for (int i = len; ; i--) 
        {  
            result += (b & 0xFF) << (8 * i);
            
            if(i == 0)
            	break;
            else
            	b = getOneByte(bf);
        }   
        return result;	
	}
	
	private static byte getOneByte(ByteBuffer bf)
	{
		if(!bf.hasRemaining())
		{
			bf.position(0);
		}
		return bf.get();
	}
	
	private static byte[] encodeBytes(byte[] data)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//null或者空串,直接用0表示
		if(data == null || data.length == 0)
		{
			return new byte[]{0}; 
		}
		
		try
		{
			//写len
			bos.write(encodeUnsignInt(data.length));
			//写内容
			bos.write(data);
		} 
		catch (IOException e)
		{//不可能到这里
			throw new RuntimeException(e.getMessage());
		}
		return bos.toByteArray();
	}
	
	private static byte[] readBytes(ByteBuffer bf)
	{
		int len = readUnsignInt(bf);
		//System.out.println("pppppppp1:"+bf.position() +" len:"+ len);
		byte[] data = new byte[len];
		if(bf.remaining() < len)
		{
			int remain = bf.remaining();
			bf.get(data, 0, remain);
			bf.position(0);
			bf.get(data, remain, len - remain);
		}
		else
		{
			bf.get(data);
		}
		return data;
	}	
	
	/* 
	 * 停止所有写操作
	 * 为了避免服务被kill掉的时候还有数据没写回导致数据错乱，在stop服务的之前调用此方法确保没有写操作正在进行
	 */
	public void stopWrite()
	{
		lock.lock();
	}
	
	public static void main(String[] args)
	{
		final PersistenQueue queue = new PersistenQueue(1024000, 102400000, ".", "queue");
		queue.clear();
		for (int i = 0; i < 10; i++)
		{
			queue.offer(new byte[]{2,3,3,3,(byte)i});
		}
		System.out.println(queue.size());
		System.out.println(Arrays.toString(queue.take()));
		System.out.println(queue.size());
		//queue.clear();
		System.out.println(queue.size());
		for (int i = 0; i < 10; i++)
		{
			queue.offer(new byte[]{4,3,3,3,(byte)i});
		}
		System.out.println(Arrays.toString(queue.take()));
		System.out.println(Arrays.toString(queue.take()));
		System.out.println(queue.size());
	}
}
