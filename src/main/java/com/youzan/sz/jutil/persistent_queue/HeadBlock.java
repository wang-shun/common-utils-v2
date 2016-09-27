package com.youzan.sz.jutil.persistent_queue;

import java.nio.ByteBuffer;

final class HeadBlock
{
	static final int SIZE = 6 * 4;
	
	int size;
	int blockCount;
	int blockSize;
	int freeHead;
	int elementHead;
	int elementTail;
	
	public HeadBlock()
	{
	}
	
	public HeadBlock(ByteBuffer bb)
	{
		readFrom(bb);
	}
	
	public HeadBlock(ByteBuffer bb, int pos)
	{
		bb.position(pos);
		readFrom(bb);
	}

	public void readFrom(ByteBuffer bb)
	{
		size = bb.getInt();
		blockCount = bb.getInt();
		blockSize = bb.getInt();
		freeHead = bb.getInt();
		elementHead = bb.getInt();
		elementTail = bb.getInt();
	}
	
	public void writeTo(ByteBuffer bb)
	{
		bb.putInt(size);
		bb.putInt(blockCount);
		bb.putInt(blockSize);
		bb.putInt(freeHead);
		bb.putInt(elementHead);
		bb.putInt(elementTail);
	}
}
