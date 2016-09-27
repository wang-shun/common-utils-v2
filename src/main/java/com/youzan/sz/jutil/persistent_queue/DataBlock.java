package com.youzan.sz.jutil.persistent_queue;

import java.nio.ByteBuffer;

final class DataBlock
{
	static final int HEAD_SIZE = 1 * 4;
	
	int nextData;
	
	public DataBlock(ByteBuffer bb)
	{
		readFrom(bb);
	}
	
	public DataBlock(ByteBuffer bb, int pos)
	{
		bb.position(pos);
		readFrom(bb);
	}
	
	public DataBlock()
	{
	}

	public void readFrom(ByteBuffer bb)
	{
		nextData = bb.getInt();
	}
	
	public void writeTo(ByteBuffer bb)
	{
		bb.putInt(nextData);
	}
}
