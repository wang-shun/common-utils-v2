package com.youzan.sz.jutil.persistent_queue;

import java.nio.ByteBuffer;

final class FirstDataBlock
{
	static final int HEAD_SIZE = 5 * 4;
	int nextData;
	int nextElement;
	int prevElement;
	int dataLenght;
	int dataTail;
	
	public FirstDataBlock(ByteBuffer bb)
	{
		readFrom(bb);
	}
	
	public FirstDataBlock(ByteBuffer bb, int pos)
	{
		bb.position(pos);
		readFrom(bb);
	}
	
	public FirstDataBlock()
	{
	}

	public void readFrom(ByteBuffer bb)
	{
		nextData = bb.getInt();
		nextElement = bb.getInt();
		prevElement = bb.getInt();
		dataLenght = bb.getInt();
		dataTail = bb.getInt();
	}
	
	public void writeTo(ByteBuffer bb)
	{
		bb.putInt(nextData);
		bb.putInt(nextElement);
		bb.putInt(prevElement);
		bb.putInt(dataLenght);
		bb.putInt(dataTail);
	}
}
