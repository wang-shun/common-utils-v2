package com.youzan.sz.jutil.persistent_queue;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.ListIterator;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.persistent_queue.DataBlock;
//import com.qq.jutil.persistent_queue.DataIterator;
//import com.qq.jutil.persistent_queue.FirstDataBlock;
//import com.qq.jutil.persistent_queue.HeadBlock;
//import com.qq.jutil.persistent_queue.LinkedList;
//import com.qq.jutil.util.Pair;

/**
 * a LinkedList in mmap
 * @author meteor
 *
 */
public class LinkedList
{
	MappedByteBuffer mb = null;
	
	static final int HEAD_SIZE = HeadBlock.SIZE;

	HeadBlock head = new HeadBlock();
	
	private int maxBlockCount;
	
	private String file;
	
	private double growRate = 2.0;
	
	private static final Logger debugLog = Logger.getLogger("jutil");

	public LinkedList(String file, int blockCount, int blockSize, int maxBlockCount) throws IOException{
		this(file, blockCount, blockSize, maxBlockCount, 2.0);
	}
	
	public LinkedList(String file, int blockCount, int blockSize, int maxBlockCount, double growRate) throws IOException{
		this.file = file;
		this.growRate = growRate;
		this.maxBlockCount = blockSize > maxBlockCount ? blockSize : maxBlockCount;
		if(blockSize <= FirstDataBlock.HEAD_SIZE)
			throw new RuntimeException("invalid argument: blockSize must larger than " + FirstDataBlock.HEAD_SIZE);
		head.blockCount = blockCount;
		head.blockSize = blockSize;
		int fileSize = HEAD_SIZE + blockCount * blockSize;
		RandomAccessFile raf = null;
		FileChannel fc = null;
		try
		{
			raf = new RandomAccessFile(file, "rw");
			fc = raf.getChannel();
			boolean needInit = raf.length() == 0;
			if (needInit)
			{
				mb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
				init();
			}
			else
			{
				fileSize = (int) raf.length();
				mb = fc.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
				head.readFrom(mb);
			}
			//ib = mb.asIntBuffer();
		}
		catch(FileNotFoundException e)
		{
			throw e;
		}
		finally
		{
			if(fc != null) 
				fc.close();
			if(raf != null)
				raf.close();
		}
	}
	
	private void init()
	{
		// init head
		ByteBuffer b = mb.duplicate();
		head.freeHead = HEAD_SIZE;
		head.elementHead = 0;
		head.elementTail = 0;
		head.size = 0;
		head.writeTo(b);
		
		// init free list
		DataBlock db = new DataBlock();
		int base = HEAD_SIZE;
		for(int i = 0; i < head.blockCount - 1; ++i)
		{
			int nextPos = base + (i + 1) * head.blockSize;
			db.nextData = nextPos;
			db.writeTo(b);
			b.position(nextPos);
		}
		b.position(base + (head.blockCount - 1) * head.blockSize);
		db.nextData = 0;
		db.writeTo(b);
	}
	
	private void pushFreeNodeList(int freeHead, int freeTail)
	{
		ByteBuffer b = mb.duplicate();
		DataBlock db = new DataBlock();
		db.nextData = head.freeHead;
		b.position(freeTail);
		db.writeTo(b);
		head.freeHead = freeHead;
		b.position(0);
		head.writeTo(b);
	}
	
	private void pushFreeNodeListToTail(int freeHead, int freeTail, int freeListTailPos)
	{
		if(freeListTailPos <= 0)
		{
			pushFreeNodeList(freeHead, freeTail);
		}
		else
		{
			ByteBuffer b = mb.duplicate();
			DataBlock db = new DataBlock();
			
			db.nextData = 0;
			b.position(freeTail);
			db.writeTo(b);
			
			db.nextData = freeHead;
			b.position(freeListTailPos);
			db.writeTo(b);
		}
	}
	
	/**
	 * 获取空闲块链（不包括首节点）
	 * @param freeNodePos	当前空闲块位置
	 * @param data			数据
	 * @param offset		数据偏移量
	 * @param length		大小
	 * @return	dataTail，最后一个块的位置
	 */
	private int popFreeNode1(int freeNodePos, byte[] data, int offset, int length)
	{
		if(freeNodePos <= 0){
			grow();
			throw new RuntimeException("not enought free node.");
		}
		final int dataSize = head.blockSize - DataBlock.HEAD_SIZE;
		final int writeBytes = length > dataSize ? dataSize : length;
		
		ByteBuffer b = mb.duplicate();
		b.position(freeNodePos + DataBlock.HEAD_SIZE);
		b.put(data, offset, writeBytes);
		if(length > writeBytes){
			DataBlock db = new DataBlock(b, freeNodePos);
			if(db.nextData <= 0){
				grow(freeNodePos);
				db = new DataBlock(b, freeNodePos);
			}
			return popFreeNode1(db.nextData, data, offset + writeBytes, length - writeBytes);
		}
		return freeNodePos;
	}
	
	private boolean grow(int freeListTailPos)
	{
		if(maxBlockCount <= head.blockCount)
			throw new RuntimeException("not enought free node.");
		int newBlockCount = (int) (head.blockCount * this.growRate);
		newBlockCount = newBlockCount > maxBlockCount ? maxBlockCount : newBlockCount;
		if(!growTo(newBlockCount, freeListTailPos))
			throw new RuntimeException("not enought free node.");
		return true;
	}
	
	private boolean grow()
	{
		return grow(-1);
	}
	/*
	private boolean growTo(int newBlockCount)
	{
		return growTo(newBlockCount, -1);
	}
	*/
	private boolean growTo(int newBlockCount, int freeListTailPos)
	{
		if(newBlockCount <= head.blockCount)
			return false;
		int newFileSize = HEAD_SIZE + newBlockCount * head.blockSize;
		int oldBlockCount = head.blockCount;
		RandomAccessFile raf = null;
		FileChannel fc = null;
		try{
			raf = new RandomAccessFile(file, "rw");
			fc = raf.getChannel();
			//newFileSize = (int) raf.length();
			mb = fc.map(FileChannel.MapMode.READ_WRITE, 0, newFileSize);
			head.readFrom(mb);

			head.blockCount = newBlockCount;
			ByteBuffer b = mb.duplicate();
			// init free list
			DataBlock db = new DataBlock();
			int base = HEAD_SIZE;
			b.position(base + oldBlockCount * head.blockSize);
			for(int i = oldBlockCount; i < newBlockCount - 1; ++i)
			{
				int nextPos = base + (i + 1) * head.blockSize;
				db.nextData = nextPos;
				db.writeTo(b);
				b.position(nextPos);
			}
			b.position(base + (head.blockCount - 1) * head.blockSize);
			db.nextData = 0;
			db.writeTo(b);
			//pushFreeNodeList(base + oldBlockCount * head.blockSize, base + (newBlockCount - 1) * head.blockSize);
			pushFreeNodeListToTail(base + oldBlockCount * head.blockSize,
					base + (newBlockCount - 1) * head.blockSize, 
					freeListTailPos);
		}
		catch (IOException e)
		{
			debugLog.error("", e);
			return false;
		}finally{
			try
			{
				fc.close();
			}
			catch (IOException e)
			{
				debugLog.error("", e);
			}
			try
			{
				raf.close();
			}
			catch (IOException e)
			{
				debugLog.error("", e);
			}
		}
		return true;
	}
	
	Pair<Integer, FirstDataBlock> popFreeNode(byte[] data, int offset, int length)
	{
		final int firstDataSize = head.blockSize - FirstDataBlock.HEAD_SIZE;
		final int writeBytes = length > firstDataSize ? firstDataSize : length;
		
		// 检查空闲链
		int freeHead = head.freeHead;
		if(freeHead == 0)
		{
			grow();
			freeHead = head.freeHead;
		}
		
		ByteBuffer b = mb.duplicate();
		//b.position(freeHead + FirstDataBlock.HEAD_SIZE);
		//b.put(data, offset, writeBytes);
		int dataTail = freeHead;
		int nextData = 0;
		if(length > writeBytes){
			DataBlock db = new DataBlock(b, freeHead);
			nextData = db.nextData;
			if(nextData <= 0){
				grow(freeHead);
				db = new DataBlock(b, freeHead);
				nextData = db.nextData;
			}
			dataTail = popFreeNode1(nextData, data, offset + writeBytes, length - writeBytes);
		}
		b.position(freeHead + FirstDataBlock.HEAD_SIZE);
		b.put(data, offset, writeBytes);
		FirstDataBlock fdb = new FirstDataBlock();
		fdb.dataLenght = length;
		fdb.dataTail = dataTail;
		fdb.nextData = nextData;
		return Pair.makePair(dataTail, fdb);
	}
	
	/**
	 * add an element before the position
	 * @param nextPos	next position
	 * @param data		data
	 * @return			the new element's position
	 */
	int addBeforePos(int nextPos, byte[] data)
	{
		Pair<Integer, FirstDataBlock> pr = popFreeNode(data, 0, data.length);
		int freeHead = head.freeHead;
		int dataTail = pr.first;
		
		ByteBuffer b = mb.duplicate();
		
		// read new element's dataTail
		DataBlock db = new DataBlock(b, dataTail);
		int nextFree = db.nextData;
		
		// set first block
		FirstDataBlock fdb = pr.second;
		int prevPos = 0;
		if(nextPos > 0){
			FirstDataBlock fdbNext = new FirstDataBlock(b, nextPos);
			fdb.prevElement = fdbNext.prevElement;
			fdb.nextElement = nextPos;
			prevPos = fdbNext.prevElement;
			fdbNext.prevElement = freeHead;
			b.position(nextPos);
			fdbNext.writeTo(b);
		}else{
			fdb.prevElement = head.elementTail;
			fdb.nextElement = 0;
			prevPos = head.elementTail;
			head.elementTail = freeHead;
		}
		if(prevPos > 0){
			FirstDataBlock fdbPrev = new FirstDataBlock(b, prevPos);
			fdbPrev.nextElement = freeHead;
			b.position(prevPos);
			fdbPrev.writeTo(b);
		}else{
			head.elementHead = freeHead;
		}
		b.position(freeHead);
		fdb.writeTo(b);
		
		// set head
		head.freeHead = nextFree;
		++head.size;
		b.position(0);
		head.writeTo(b);

		// set new element's tail dataNext
		//db.nextData = nextPos;		// TODO 有问题,已修改
		db.nextData = 0;
		b.position(dataTail);
		db.writeTo(b);
		
		return freeHead;
	}
	
	public void add(byte[] data)
	{
		addFirst(data);
	}
	
	public void addAll(Collection<byte[]> c)
	{
		for (byte[] bs : c)
			add(bs);
	}
	
	public void addFirst(byte[] data)
	{
		addBeforePos(head.elementHead, data);
	}
	
	public void addLast(byte[] data)
	{
		addBeforePos(0, data);
	}
	
	public void appendByPosition(int elementPos, byte[] appendData)
	{
		if(elementPos <= 0)
			throw new IndexOutOfBoundsException();
		ByteBuffer b = mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, elementPos);
		if(fdb.dataTail == elementPos){
			// 第一个Block还没用完
			int left = head.blockSize - FirstDataBlock.HEAD_SIZE - fdb.dataLenght;
			if(left >= appendData.length){
				// 第一个Block剩余的空间足够append
				b.position(elementPos + FirstDataBlock.HEAD_SIZE + fdb.dataLenght);
				b.put(appendData);
				fdb.dataLenght += appendData.length;
				b.position(elementPos);
				fdb.writeTo(b);
			}else{
				// 先写满第一个Block
				b.position(elementPos + FirstDataBlock.HEAD_SIZE + fdb.dataLenght);
				b.put(appendData, 0, left);
				
				// 检查空闲链
				int freeHead = head.freeHead;
				if(freeHead == 0)
				{
					grow();
					freeHead = head.freeHead;
				}

				/*
				// 继续分配新的Block，并写入
				int dataTail = freeHead;
				DataBlock db = new DataBlock(b, freeHead);
				int nextData = db.nextData;
				if(nextData <= 0){
					grow();
					db = new DataBlock(b, freeHead);
					nextData = db.nextData;
				}
				dataTail = popFreeNode1(nextData, 
						appendData,
						left, 
						appendData.length - left);
				fdb.dataLenght += appendData.length;
				fdb.dataTail = dataTail;
				fdb.nextData = nextData;
				b.position(elementPos);
				fdb.writeTo(b);
				*/
				int dataTail = popFreeNode1(freeHead, 
						appendData,
						left, 
						appendData.length - left);
				DataBlock db = new DataBlock(b, dataTail);
				int nextFree = db.nextData;
				head.freeHead = nextFree;
				b.position(0);
				head.writeTo(b);
				db.nextData = 0;
				b.position(dataTail);
				db.writeTo(b);
				
				fdb.dataLenght += appendData.length;
				fdb.dataTail = dataTail;
				fdb.nextData = freeHead;
				b.position(elementPos);
				fdb.writeTo(b);
			}
		}else{
			int len1 = head.blockSize - FirstDataBlock.HEAD_SIZE;
			int len2 = head.blockSize - DataBlock.HEAD_SIZE;
			int last = (fdb.dataLenght - len1) % len2;
			last = last == 0 ? len2 : last;
			int dataTail = fdb.dataTail;
			
			int left = len2 - last;
			if(left >= appendData.length){
				// 最后一个Block剩余的空间已经足够
				b.position(dataTail + DataBlock.HEAD_SIZE + last);
				b.put(appendData, 0, appendData.length);
				
				fdb.dataLenght += appendData.length;
				b.position(elementPos);
				fdb.writeTo(b);
			}else{			
				// 先写满最后一个Block
				b.position(dataTail + DataBlock.HEAD_SIZE + last);
				b.put(appendData, 0, len2 - last);
				
				// 检查空闲链
				int freeHead = head.freeHead;
				if(freeHead == 0)
				{
					grow();
					b = mb.duplicate();
					freeHead = head.freeHead;
				}

				/*
				DataBlock dbDataTailOld = new DataBlock(b, dataTail);
				
				// 继续分配新的Block，并写入
				dataTail = freeHead;
				DataBlock db = new DataBlock(b, freeHead);
				int nextData = db.nextData;
				dataTail = popFreeNode1(nextData, 
						appendData,
						len2 - last, 
						appendData.length - (len2 - last));
				dbDataTailOld.nextData = nextData;
				b.position(fdb.dataTail);
				dbDataTailOld.writeTo(b);
				fdb.dataLenght += appendData.length;
				fdb.dataTail = dataTail;
				b.position(elementPos);
				fdb.writeTo(b);
				*/
				dataTail = popFreeNode1(freeHead, 
						appendData,
						len2 - last, 
						appendData.length - (len2 - last));
				DataBlock db = new DataBlock(b, dataTail);
				int nextFree = db.nextData;
				head.freeHead = nextFree;
				b.position(0);
				head.writeTo(b);
				db.nextData = 0;
				b.position(dataTail);
				db.writeTo(b);
				
				DataBlock dbDataTailOld = new DataBlock(b, dataTail);
				dbDataTailOld.nextData = freeHead;
				b.position(fdb.dataTail);
				dbDataTailOld.writeTo(b);
				fdb.dataLenght += appendData.length;
				fdb.dataTail = dataTail;
				b.position(elementPos);
				fdb.writeTo(b);
			}
		}
	}
	
	byte[] getDataByPos(int elementPos)
	{
		if(elementPos <= 0)
			throw new IndexOutOfBoundsException();
		ByteBuffer b = mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, elementPos);
		byte[] bs = new byte[fdb.dataLenght];
		if(bs.length == 0)
			return bs;
		int curBlockPos = elementPos;
		int left = bs.length;
		final int dataSize = head.blockSize - DataBlock.HEAD_SIZE;
		final int firstDataSize = head.blockSize - FirstDataBlock.HEAD_SIZE;
		int readBytes = left > firstDataSize ? firstDataSize : left;
		b.position(elementPos + FirstDataBlock.HEAD_SIZE);
		do{
			b.get(bs, bs.length - left, readBytes);
			left -= readBytes;
			readBytes = left > dataSize ? dataSize : left;
			DataBlock db = new DataBlock(b, curBlockPos);
			curBlockPos = db.nextData;
			b.position(curBlockPos + DataBlock.HEAD_SIZE);
		} while(left > 0 && curBlockPos > 0);

		return bs;
	}
	
	public byte[] getFirst()
	{
		return getDataByPos(head.elementHead);
	}
	
	public byte[] getLast()
	{
		return getDataByPos(head.elementTail);
	}
	
	void removeByPos(int elementPos)
	{
		if(head.size <= 0)
		{
			throw new RuntimeException("removeByPos error: LinkedList is empty");
		}
		ByteBuffer b = mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, elementPos);
		if(fdb.nextElement > 0){
			FirstDataBlock fdbNext = new FirstDataBlock(b, fdb.nextElement);
			fdbNext.prevElement = fdb.prevElement;
			b.position(fdb.nextElement);
			fdbNext.writeTo(b);
		}else{
			head.elementTail = fdb.prevElement;
		}
		if(fdb.prevElement > 0){
			FirstDataBlock fdbPrev = new FirstDataBlock(b, fdb.prevElement);
			fdbPrev.nextElement = fdb.nextElement;
			b.position(fdb.prevElement);
			fdbPrev.writeTo(b);
		}else{
			head.elementHead = fdb.nextElement;
		}
		--head.size;
		pushFreeNodeList(elementPos, fdb.dataTail);
		//b.position(0);
		//head.writeTo(b);
	}
	
	public byte[] removeFirst()
	{
		byte[] bs = getDataByPos(head.elementHead);
		removeByPos(head.elementHead);
		return bs;
	}
	
	public byte[] removeLast()
	{
		byte[] bs = getDataByPos(head.elementTail);
		removeByPos(head.elementTail);
		return bs;
	}
	
	public int size()
	{
		return head.size;
	}
	
	public boolean isEmpty()
	{
		return size() > 0;
	}
	
	public DataIterator iterator()
	{
		return new DataIterator(this);
	}
	
	public DataIterator iterator(int index)
	{
		return new DataIterator(this, index);
	}
	
	public void clear()
	{
		init();
	}
	
	public String getFilename()
	{
		return file;
	}
	
	public static void main(String[] args) throws IOException
	{
		LinkedList ll = new LinkedList("d:/ll.dat", 2, 32, 64);
		byte[] bs = "abcdefghijklmnopqrstuvwxyz1".getBytes();
		//*
		ll.addFirst("abcdefghijklmnopqrstuvwxyz2".getBytes());
		ll.addFirst("abcdefghijklmnopqrstuvwxyz3".getBytes());
		ll.addLast("abcdefghijklmnopqrstuvwxyz4".getBytes());
		//*/
		ListIterator<byte[]> it = ll.iterator();
		//it.add("add".getBytes());
		//int i = 0;
		while(it.hasNext()){
			bs = it.next();
			/*
			if(++i % 3 == 0){
				it.remove();
			}else if(i % 3 == 1){
				it.add("12kaaaaaaaaaaaaaaaaaaaaaaaaaa3".getBytes());
			}else{
				it.set("setafffffffffffffffffffffffffffffffffff".getBytes());
			}
			//*/
			System.out.println(new String(bs));
		}
		System.exit(0);
	}
}
