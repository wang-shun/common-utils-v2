package com.youzan.sz.jutil.bytes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Debyter
{
//---------int
	public static int getInt(ByteBuffer bb)
	{
		return bb.getInt();
	}
	
	public static List<Integer> getIntList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<Integer> list = new ArrayList<Integer>();
		while(bb.hasRemaining())
		{
			list.add(bb.getInt());
		}
		return list;
	}
	
	public static List<Integer> getIntList(ByteBuffer src)
	{
		return getIntList(src, true);
	}
//---------short
	public static short getShort(ByteBuffer bb)
	{
		return bb.getShort();
	}
	
	public static List<Short> getShortList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<Short> list = new ArrayList<Short>();
		while(bb.hasRemaining())
		{
			list.add(bb.getShort());
		}
		return list;
	}
	
	public static List<Short> getShortList(ByteBuffer src)
	{
		return getShortList(src, true);
	}	
//-------long	
	public static long getLong(ByteBuffer bb)
	{
		return bb.getLong();
	}
	public static List<Long> getLongList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<Long> list = new ArrayList<Long>();
		while(bb.hasRemaining())
		{
			list.add(bb.getLong());
		}
		return list;
	}	
	public static List<Long> getLongList(ByteBuffer src)
	{
		return getLongList(src, true);
	}
//------boolean	
	public static boolean getBoolean(ByteBuffer bb)
	{
		return (bb.get() & 0xFF) != 0;
	}
	
	public static List<Boolean> getBooleanList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<Boolean> list = new ArrayList<Boolean>();
		while(bb.hasRemaining())
		{
			list.add((bb.get() & 0xFF) != 0);
		}
		return list;
	}
	public static List<Boolean> getBooleanList(ByteBuffer src)
	{
		return getBooleanList(src, true);
	}
//-------bytes	
	public static byte[] getBytes(ByteBuffer bb)
	{
		int len = bb.getInt();
		if(len < 0)
		{
			return null;
		}
		byte[] bs = new byte[len];
		bb.get(bs);
		return bs;
	}
	
	public static List<byte[]> getBytesList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<byte[]> list = new ArrayList<byte[]>();
		while(bb.hasRemaining())
		{
			list.add(getBytes(bb));
		}
		return list;
	}
	public static List<byte[]> getBytesList(ByteBuffer src)
	{
		return getBytesList(src, true);
	}
//	-------String	
	public static String getString(ByteBuffer bb)
	{
		int len = bb.getInt();
		//if(len < -1)//why -1?
		if(len < 0)
		{
			return null;
		}
		byte[] bs = new byte[len];
		bb.get(bs);
		return ByteUtil.recoverString(bs);
	}
	
	public static List<String> getStringList(ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<String> list = new ArrayList<String>();
		while(bb.hasRemaining())
		{
			list.add(ByteUtil.recoverString(getBytes(bb)));
		}
		return list;
	}
	public static List<String> getStringList(ByteBuffer src)
	{
		return getStringList(src, true);
	}
//------bytesable
	public static <T extends Bytesable> T getBytesable(Class<T> clazz, ByteBuffer src)
	{
		byte[] bs = getBytes(src);
		if(bs == null)
		{
			return null;
		}
		return ByteUtil.recoverBytesable(clazz,bs);
	}
	
	public static <T extends Bytesable> List<T> getBytesableList(Class<T> clazz, ByteBuffer src, boolean withLen)
	{
		ByteBuffer bb = getDataBuffer(src, withLen);
		if(bb == null) 
			return null;
		List<T> list = new ArrayList<T>();
		while(bb.hasRemaining())
		{
			list.add(getBytesable(clazz, bb));
		}
		return list;
	}
	public static <T extends Bytesable> List<T> getBytesableList(Class<T> clazz, ByteBuffer src)
	{
		return getBytesableList(clazz, src, true);
	}
	//---------------------
	private static ByteBuffer getDataBuffer(ByteBuffer src, boolean withLen)
	{
		if(withLen)
		{
			byte[] bs = getBytes(src);
			if(bs == null)
			{
				return null;
			}
			return ByteBuffer.wrap(bs);
		}
		else
		{
			return src;
		}
	}
	
	public static void main(String[] args)
	{
		test();
	}
	private static void test()
	{
		//int
		System.out.println("------test int-------");
		ArrayList<Integer> list1 = new ArrayList<Integer>();
		list1.add(1);
		list1.add(2);
		list1.add(3);
		System.out.println(Debyter.getIntList(ByteBuffer.wrap(ByteUtil.enbyteIntList(list1, false)), false));
		System.out.println(Debyter.getIntList(ByteBuffer.wrap(ByteUtil.enbyteIntList(list1, true)), true));
		//short
		System.out.println("------test short-------");
		ArrayList<Short> list2 = new ArrayList<Short>();
		list2.add((short)1);
		list2.add((short)2);
		list2.add((short)3);
		System.out.println(Debyter.getShortList(ByteBuffer.wrap(ByteUtil.enbyteShortList(list2, false)), false));
		System.out.println(Debyter.getShortList(ByteBuffer.wrap(ByteUtil.enbyteShortList(list2, true)), true));
		//long
		System.out.println("------test long-------");
		ArrayList<Long> list3 = new ArrayList<Long>();
		list3.add(1L);
		list3.add(2L);
		list3.add(3L);
		System.out.println(Debyter.getLongList(ByteBuffer.wrap(ByteUtil.enbyteLongList(list3, false)), false));
		System.out.println(Debyter.getLongList(ByteBuffer.wrap(ByteUtil.enbyteLongList(list3, true)), true));		
		//boolean
		System.out.println("------test boolean-------");
		ArrayList<Boolean> list4 = new ArrayList<Boolean>();
		list4.add(true);
		list4.add(false);
		list4.add(true);
		System.out.println(Debyter.getBooleanList(ByteBuffer.wrap(ByteUtil.enbyteBooleanList(list4, false)), false));
		System.out.println(Debyter.getBooleanList(ByteBuffer.wrap(ByteUtil.enbyteBooleanList(list4, true)), true));		
		//string
		System.out.println("------test string-------");
		ArrayList<String> list5 = new ArrayList<String>();
		list5.add("a");
		list5.add("b");
		list5.add("c");
		System.out.println(Debyter.getStringList(ByteBuffer.wrap(ByteUtil.enbyteStringList(list5, false)), false));
		System.out.println(Debyter.getStringList(ByteBuffer.wrap(ByteUtil.enbyteStringList(list5, true)), true));		
		//bytes
		System.out.println("------test bytes-------");
		ArrayList<byte[]> list6 = new ArrayList<byte[]>();
		list6.add(new byte[]{1,2,3});
		list6.add(new byte[]{4,5,6});
		list6.add(new byte[]{7,8,9});
		System.out.println(Debyter.getBytesList(ByteBuffer.wrap(ByteUtil.enbyteBytesList(list6, false)), false));
		System.out.println(Debyter.getBytesList(ByteBuffer.wrap(ByteUtil.enbyteBytesList(list6, true)), true));		
		//bytesable
		System.out.println("------test bytesable-------");
		ArrayList<BitBuffer> list7 = new ArrayList<BitBuffer>();
		list7.add(new BitBuffer(new byte[]{1,1,1}));
		list7.add(new BitBuffer(new byte[]{2,2,2}));
		list7.add(new BitBuffer(new byte[]{3,3,3}));
		System.out.println(Debyter.getBytesableList(BitBuffer.class, ByteBuffer.wrap(ByteUtil.enbyteList(list7, false)), false));
		System.out.println(Debyter.getBytesableList(BitBuffer.class, ByteBuffer.wrap(ByteUtil.enbyteList(list7, true)), true));
	}	
}
