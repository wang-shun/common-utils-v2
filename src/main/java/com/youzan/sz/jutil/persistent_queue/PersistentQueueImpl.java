package com.youzan.sz.jutil.persistent_queue;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.util.Pair;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.persistent_queue.DataIterator;
//import com.qq.jutil.persistent_queue.LinkedList;
//import com.qq.jutil.persistent_queue.PersistentQueueImpl;
//import com.qq.jutil.util.Pair;

final class PersistentQueueImpl
{
	private LinkedList ls;
	
	private static final int BLOCK_COUNT = 256;
	//private static final int BLOCK_COUNT = 16;
	
	private static final int BLOCK_SIZE = 1024;
	//private static final int BLOCK_SIZE = 32;
	
	private static final int MAX_BLOCK_COUNT = 1024 * 1024;
	//private static final int MAX_BLOCK_COUNT = 1024 * 1024;
	
	private static final int MAX_KEY_LENGTH = 1024 * 1024 * 10; // 10M
	
	private static final Logger debugLog = Logger.getLogger("jutil");
	
	private Map<String, Integer> map = new ConcurrentHashMap<String, Integer>();
	
	private String file;
	private int maxQueueSize;
	PersistentQueueImpl(String file, int initBlockCount, int blockSize, int maxBlockCount, int maxQueueSize) throws IOException
	{
		this.file = file;
		this.maxQueueSize = maxQueueSize;
		ls = new LinkedList(file, initBlockCount, blockSize, maxBlockCount);
		
		// init map
		DataIterator it = ls.iterator();
		while(it.hasNext()){
			int curPos = it.curPos;
			byte[] v = it.next();
			byte[] key = parseKey(v);
			if(key != null)
				map.put(new String(key), curPos);
		}
	}
	PersistentQueueImpl(String file, int maxQueueSize) throws IOException
	{
		this(file, BLOCK_COUNT, BLOCK_SIZE, MAX_BLOCK_COUNT, maxQueueSize);
	}

	private byte[] parseKey(byte[] v)
	{
		ByteBuffer bb = ByteBuffer.wrap(v);
		int keyLength = bb.getInt();
		if(keyLength < 0 || keyLength > MAX_KEY_LENGTH || keyLength > v.length - bb.position()){
			return null;
		}
		byte[] key = new byte[keyLength];
		bb.get(key);
		return key;
	}
	
	private Pair<String, byte[]> parse(byte[] v)
	{
		ByteBuffer bb = ByteBuffer.wrap(v);
		int keyLength = bb.getInt();
		if(keyLength < 0 || keyLength > MAX_KEY_LENGTH || keyLength > v.length - bb.position()){
			return null;
		}
		byte[] key = new byte[keyLength];
		bb.get(key);
		byte[] value = new byte[v.length - bb.position()];
		bb.get(value);
		return Pair.makePair(new String(key), value);
	}
	
	private byte[] combin(byte[] key, byte[] value)
	{
		ByteBuffer bb = ByteBuffer.allocate(4 + key.length + value.length);
		bb.putInt(key.length);
		bb.put(key);
		bb.put(value);
		return bb.array();
	}
	
	public Pair<String, byte[]> pop()
	{
		if(isEmpty())
			return null;
		Pair<String, byte[]> pr = null;
		try{
			byte[] v = ls.removeFirst();
			pr = parse(v);
			Integer i = map.remove(pr.first);
			if(i == null)
				debugLog.error("i: " + i);
		}catch(Exception e){
			debugLog.error("pop error, map.size(): " + map.size() + "\tfile: " + file 
					+ "\tmap.keys: " + Arrays.toString(map.keySet().toArray()) + "\tmap.values: " + Arrays.toString(map.values().toArray()),
					e);
			/*
			System.err.println("pop error, map.size(): " + map.size() + "\tfile: " + file 
					+ "\tmap.keys: " + Arrays.toString(map.keySet().toArray()) + "\tmap.values: " + Arrays.toString(map.values().toArray()));
			e.printStackTrace();
			*/
		}
		if(map.size() != ls.size()){
			debugLog.error("[pop]size error, map.size: " + map.size() + "\tls.size: " + ls.size()
					 + "\tfile: " + file  + "\tpr.first: " + pr.first
						+ "\tmap.keys: " + Arrays.toString(map.keySet().toArray()) + "\tmap.values: " + Arrays.toString(map.values().toArray()));
			/*
			System.err.println("[pop]size error, map.size: " + map.size() + "\tls.size: " + ls.size()
					 + "\tfile: " + file  + "\tpr.first: " + pr.first
					+ "\tmap.keys: " + Arrays.toString(map.keySet().toArray()) + "\tmap.values: " + Arrays.toString(map.values().toArray()));
			*/
			//System.exit(0);
		}
		return pr;
	}
	
	public void put(String key, byte[] value)
	{
		Integer pos = map.get(key);
		if(pos != null)
		{
			map.remove(key);
			ls.removeByPos(pos);
		}
		else
		{//霄1�7要新加item,先判断是否到达max size亄1�7
			if(this.maxQueueSize > 0 && this.map.size() >= this.maxQueueSize)
			{
				throw new RuntimeException("[PersistentQueueImpl]put,too manny items:"+ this.map.size() +"\tmaxSize:"+ this.maxQueueSize);
			}
		}
		byte[] v = combin(key.getBytes(), value);
		int p2 = ls.addBeforePos(0, v);
		map.put(key, p2);
		if(map.size() != ls.size())
		{
			debugLog.error("[put]size error, map.size: " + map.size() + "\tls.size: " + ls.size());
			//System.err.println("[put]size error, map.size: " + map.size() + "\tls.size: " + ls.size());
		}
	}
	
	public void append(String k, byte[] appendData)
	{
		byte[] key = k.getBytes();
		Integer pos = map.get(k);
		if(pos != null){
			/*
			byte[] bs = ls.getDataByPos(pos);
			ByteBuffer bb = ByteBuffer.allocate(bs.length + appendData.length);
			bb.put(bs);
			bb.put(appendData);
			ls.removeByPos(pos);
			int p2 = ls.addBeforePos(0, bb.array());
			map.put(k, p2);
			*/
			ls.appendByPosition(pos, appendData);
		}else{
			byte[] v = combin(key, appendData);
			int p2 = ls.addBeforePos(0, v);
			map.put(k, p2);
		}
		if(map.size() != ls.size())
			debugLog.error("[append]size error, map.size: " + map.size() + "\tls.size: " + ls.size());
	}
	
	public void remove(String key)
	{
		Integer pos = map.get(key);
		if(pos == null)
			return;
		map.remove(key);
		ls.removeByPos(pos);
		if(map.size() != ls.size())
			debugLog.error("[remove]size error, map.size: " + map.size() + "\tls.size: " + ls.size());
	}
	
	public byte[] get(String key)
	{
		Integer pos = map.get(key);
		if(pos == null)
			return null;
		byte[] bs = ls.getDataByPos(pos);
		return parse(bs).second;
	}
	
	public boolean contain(String key)
	{
		return map.get(key) != null;
	}
	
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	public int size()
	{
		return map.size();
	}
	
	public int hashCode()
	{
		return file.hashCode();
	}
	
	public boolean equals(Object o)
	{
		PersistentQueueImpl wb = (PersistentQueueImpl) o;
		if(wb == null)
			return false;
		return file.equals(wb.file);
	}
	
	public String toString()
	{
		return super.toString();
	}
	
	@SuppressWarnings("unchecked")
	public Pair<String, byte[]>[] toArray()
	{
		int s = size();
		Pair<String, byte[]>[] pr = new Pair[s];
		DataIterator it = ls.iterator();
		for(int i = 0; i < pr.length && it.hasNext(); ++i){
			pr[i] = parse(it.next());
		}
		return pr;
	}
}
