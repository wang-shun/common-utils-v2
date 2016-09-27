package com.youzan.sz.jutil.persistent_queue;

import java.nio.ByteBuffer;
import java.util.ListIterator;

//import com.qq.jutil.persistent_queue.DataIterator;
//import com.qq.jutil.persistent_queue.FirstDataBlock;
//import com.qq.jutil.persistent_queue.LinkedList;

final class DataIterator implements ListIterator<byte[]>
{
	private LinkedList ll;
	
	int curPos;
	
	private int curIdx;
	
	public DataIterator(LinkedList ll, int idx)
	{
		this(ll);
		if(idx < 0 || idx >= ll.size())
			throw new IndexOutOfBoundsException("idx: " + idx + "\tsize: " + ll.size());
		for(int i = 0; i < idx; ++i){
			++curIdx;
			ByteBuffer b = ll.mb.duplicate();
			FirstDataBlock fdb = new FirstDataBlock(b, curPos);
			curPos = fdb.nextElement;
		}
	}
	
	public DataIterator(LinkedList ll)
	{
		this.ll = ll;
		this.curPos = ll.head.elementHead;
		this.curIdx = 0;
	}
	
	public Object clone()
	{
		DataIterator di = new DataIterator(ll);
		di.curIdx = this.curIdx;
		di.curPos = this.curPos;
		return di;
	}
	
	public boolean hasNext()
	{
		return curPos > 0;
	}
	
	public byte[] next()
	{
		if(++curIdx > ll.size())
			throw new DataFileInvalidException("linked list file invalid: " + ll.getFilename() + ", curId > list.size()");
		ByteBuffer b = ll.mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, curPos);
		byte[] bs = ll.getDataByPos(curPos);
		curPos = fdb.nextElement;
		return bs;
	}

	public void remove()
	{
		ByteBuffer b = ll.mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, curPos);
		ll.removeByPos(curPos);
		curPos = fdb.nextElement;
	}

	public boolean hasPrevious()
	{
		if(curPos <= 0)
			return false;
		ByteBuffer b = ll.mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, curPos);
		return fdb.prevElement > 0;
	}

	public byte[] previous()
	{
		if(--curIdx < 0)
			throw new DataFileInvalidException("linked list file invalid: " + ll.getFilename() + ", curId < 0");
		ByteBuffer b = ll.mb.duplicate();
		FirstDataBlock fdb = new FirstDataBlock(b, curPos);
		curPos = fdb.prevElement;
		return ll.getDataByPos(curPos);
	}

	public int nextIndex()
	{
		return hasNext() ? curPos : -1;
	}

	public int previousIndex()
	{
		return hasPrevious() ? curPos : -1;
	}

	public void set(byte[] data)
	{
		remove();
		add(data);
	}

	public void add(byte[] data)
	{
		ll.addBeforePos(curPos, data);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
	}
}
