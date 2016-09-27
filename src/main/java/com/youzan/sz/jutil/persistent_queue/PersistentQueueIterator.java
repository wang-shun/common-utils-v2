package com.youzan.sz.jutil.persistent_queue;

import java.util.Iterator;
import java.util.Map;


class PersistentQueueIterator implements Iterator<Map.Entry<byte[], byte[]>>
{
	public boolean hasNext()
	{
		return false;
	}

	public Map.Entry<byte[], byte[]> next()
	{
		return null;
	}

	public void remove()
	{
		
	}

	public static void main(String[] args)
	{
	}
}
