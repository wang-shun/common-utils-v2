package com.youzan.sz.jutil.bytes;

import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

//import com.qq.jutil.crypto.HexUtil;

public class ByteUtil
{
	public static final String DEFAULT_ENCODE = "UTF-8";
//	int	
	public static byte[] enbyteInt(int value)
	{
		byte[] data = new byte[4]; 
		for ( int i = 0; i < 4; i++) 
		{ 
			data[3 - i] = (byte)(value >> 8*i & 0xFF );    
		} 
		return data; 
	}	
	
	public static int debyteInt(byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteInt]Input bytes can not be null.");
		}
		if(data.length != Bytesable.INTEGER)
		{
			throw new DebyteException("[debyteInt]Input bytes size must be 4.");
		}
        int result = 0; 
        byte b; 
         
        for ( int i = 0; i < 4 ; i++) 
        { 
        	b = data[3 - i]; 
            result += (b & 0xFF) << (8 * i);    
        }   
        return result;		
	}
	
	public static byte[] enbyteIntList(List<Integer> array, boolean withLen)
	{
		return enbyteList(Bytesable.INTEGER, array, withLen);
	}
	
	public static byte[] enbyteIntList(List<Integer> array)
	{
		return enbyteIntList(array, true);
	}

	public static List<Integer> debyteIntList(byte[] data, boolean withLen)
	{
		return Debyter.getIntList(ByteBuffer.wrap(data), withLen);
	}
	
	public static List<Integer> debyteIntList(byte[] data)
	{
		return debyteIntList(data, true);
	}
//	short	
	public static byte[] enbyteShort(short value)
	{
		byte[] data = new byte[2]; 
		for ( int i = 0; i < 2; i++) 
		{ 
			data[1 - i] = (byte)(value >> 8*i & 0xFF );    
		} 
		return data;  
	}	
	
	public static short debyteShort(byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteShort]Input bytes can not be null.");
		}
		if(data.length != Bytesable.SHORT)
		{
			throw new DebyteException("[debyteShort]Input bytes size must be 2.");
		}
        short result = 0; 
        byte b; 
         
        for ( int i = 0; i < 2 ; i++) 
        { 
        	b = data[1 - i]; 
            result += (b & 0xFF) << (8 * i);    
        }   
        return result;	
	}
	
	public static byte[] enbyteShortList(List<Short> array, boolean withLen)
	{
		return enbyteList(Bytesable.SHORT, array, withLen);
	}
	
	public static byte[] enbyteShortList(List<Short> array)
	{
		return enbyteShortList(array, true);
	}

	public static List<Short> debyteShortList(byte[] data, boolean withLen)
	{
		return Debyter.getShortList(ByteBuffer.wrap(data), withLen);
	}
	
	public static List<Short> debyteShortList(byte[] data)
	{
		return debyteShortList(data, true);
	}
//long	
	public static byte[] enbyteLong(long value)
	{	
		byte[] data = new byte[8]; 
		for ( int i = 0; i < 8; i++) 
		{ 
			data[7 - i] = (byte)(value >> 8*i & 0xFF );    
		} 
		return data; 		
		//return ByteBuffer.allocate(Bytesable.LONG).putLong(value).array();
	}	
	public static long debyteLong(byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteLong]Input bytes can not be null.");
		}
		if(data.length != Bytesable.LONG)
		{
			throw new DebyteException("[debyteLong]Input bytes size must be 8.");
		}		
        long result = 0; 
        byte b; 
         
        for ( int i = 0; i < 8 ; i++) 
        { 
        	b = data[7 - i]; 
            result += (long)(b & 0xFF) << (8 * i);    
        }   
        return result;			
	}
	
	public static byte[] enbyteLongList(List<Long> array, boolean withLen)
	{
		return enbyteList(Bytesable.LONG, array, withLen);	
	}
	
	public static byte[] enbyteLongList(List<Long> array)
	{
		return enbyteLongList(array, true);
	}
	
	public static List<Long> debyteLongList(byte[] data, boolean withLen)
	{
		return Debyter.getLongList(ByteBuffer.wrap(data), withLen);
	}
	public static List<Long> debyteLongList(byte[] data)
	{
		return debyteLongList(data, true);
	}
//boolean	
	public static byte[] enbyteBoolean(boolean value)
	{
		byte[] b = new byte[]{value ? (byte)1 : (byte)0};
		return b;
		//return ByteBuffer.allocate(Bytesable.BOOLEAN).put(b).array();
	}	
	public static boolean debyteBoolean(byte data)
	{				
        return (data & 0xFF) != 0;			
	}
	
	public static byte[] enbyteBooleanList(List<Boolean> array, boolean withLen)
	{
		return enbyteList(Bytesable.BOOLEAN, array, withLen);
	}
	
	public static byte[] enbyteBooleanList(List<Boolean> array)
	{
		return enbyteBooleanList(array, true);
	}
	
	public static List<Boolean> debyteBooleanList(byte[] data, boolean withLen)
	{
		return Debyter.getBooleanList(ByteBuffer.wrap(data), withLen);
	}
	public static List<Boolean> debyteBooleanList(byte[] data)
	{
		return debyteBooleanList(data, true);
	}
	
//bytes
	public static byte[] enbyteBytes(byte[] value)
	{		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ByteBuffer b = ByteBuffer.allocate(Bytesable.INTEGER);
		if(value == null)
		{
			b.putInt(-1);
			try
			{
				bos.write(b.array());
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			b.putInt(value.length);
			try
			{
				bos.write(b.array());
				bos.write(value);			
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return bos.toByteArray();		
	}		
	
	public static byte[] debyteBytes(byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteBytes]Input bytes can not be null.");
		}	
		if(data.length < 4)
		{
			throw new DebyteException("[debyteBytes]Input bytes size must >= 4.");
		}			
		int len = ByteBuffer.wrap(data,0,Bytesable.INTEGER).asIntBuffer().get();
		if(len <= 0)
		{
			return null;
		}		
		if(len != data.length - 4)
		{
			throw new DebyteException("[debyteBytes]Input data format wrong:len: "+ len +",real len:"+ (data.length - 4));
		}			
		byte[] result = new byte[data.length - Bytesable.INTEGER];
		System.arraycopy(data, Bytesable.INTEGER, result, 0, result.length);
		return result;
	}	
	
	public static byte[] enbyteBytesList(List<byte[]> array, boolean withLen)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			if(array == null)
			{
				return withLen ? enbyteBytes(null) : null;
			}
			else if(array.size() == 0)
			{
				return withLen ? enbyteBytes(new byte[]{}) : new byte[]{};
			}
			else
			{
				for(byte[] b : array)
				{
					bos.write(enbyteBytes(b));
				}
			}
		} 
		catch (IOException e)
		{
			throw new EnbyteException("[enbyteBytesList]IOException .");
		}
		return withLen ? enbyteBytes(bos.toByteArray()) : bos.toByteArray();	
	}
	
	public static byte[] enbyteBytesList(List<byte[]> array)
	{
		return enbyteBytesList(array, true);
	}
	
	public static List<byte[]> debyteBytesList(byte[] data, boolean withLen)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteBytesList]Input bytes can not be null.");
		}
		return recoverBytesList(withLen ? debyteBytes(data) : data);
	}
	
	public static List<byte[]> debyteBytesList(byte[] data)
	{
		return debyteBytesList(data, true);
	}
	
	public static List<byte[]> recoverBytesList(byte[] bs)
	{
		if(bs == null)
		{
			return null;
		}
		if(bs.length == 0)
		{
			return new ArrayList<byte[]>();
		}
		int dataLen = bs.length;
		List<byte[]> list = new ArrayList<byte[]>();
		int begin = 0;
		while(begin < dataLen)
		{//顺序取bytes
			//取bytes的长度
			int len = ByteBuffer.wrap(bs,begin,Bytesable.INTEGER).asIntBuffer().get();
			begin += Bytesable.INTEGER;			
			if(len <= 0)
			{
				list.add(null);
			}
			else
			{	
				if(len > (dataLen - begin))
				{//超出范围
					throw new DebyteException("[recoverBytesList]Input data format wrong,begin:"
											  + begin + ",len: " + len + ",dataLen:" + dataLen + ",data:" + HexUtil.bytes2HexStr(bs));
				}
				else
				{
					byte[] bbb = new byte[len];		
					System.arraycopy(bs, begin, bbb, 0, len);
					list.add(bbb);
					begin += len;
				}
			}
		}
		return list;
	}
	
//string	
	public static byte[] enbyteString(String value)
	{
		try
		{
			return enbyteBytes(value == null ? null : value.getBytes(DEFAULT_ENCODE));
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new DebyteException("UnsupportedEncoding "+ DEFAULT_ENCODE +", encode string fail.");
		}
	}	
	
	public static  String debyteString(byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteString]Input bytes can not be null.");
		}
		byte[] bs = debyteBytes(data);
		return recoverString(bs);
	}
	
	public static String recoverString(byte[] bs)
	{
		if(bs == null)
		{
			return null;
		}
		try
		{
			return new String(bs, DEFAULT_ENCODE);
		} 
		catch (UnsupportedEncodingException e)
		{
			throw new DebyteException("UnsupportedEncoding, new string fail.");
		}
	}
	
	public static byte[] enbyteStringList(List<String> array, boolean withLen)
	{
		if(array == null)
		{
			return withLen ? enbyteBytes(null) : null;
		}
		if(array.size() == 0)
		{
			return withLen ? enbyteBytes(new byte[]{}) : new byte[]{};
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for(String str : array)
		{
			try
			{
				bos.write(enbyteString(str));
			} 
			catch (Exception e)
			{
				throw new DebyteException("[enbyteStringList]exception:"+ e.getMessage());
			}
		}
		return withLen ? enbyteBytes(bos.toByteArray()) : bos.toByteArray();
	}
	
	public static byte[] enbyteStringList(List<String> array)
	{
		return enbyteStringList(array, true);
	}
	
	public static List<String> debyteStringList(byte[] data, boolean withLen)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteStringList]Input bytes can not be null.");
		}
		return recoverStringList(withLen ? debyteBytes(data) : data);
	}
	
	public static List<String> debyteStringList(byte[] data)
	{
		return debyteStringList(data, true);
	}
	
	public static List<String> recoverStringList(byte[] data)
	{
		if(data == null)
		{
			return null;
		}
		List<String> list = new ArrayList<String>();
		List<byte[]> bList = recoverBytesList(data);
		if(bList == null)
		{
			return null;
		}
		else
		{
			for (byte[] b : bList)
			{
				list.add(new String(b));
			}
		}
		return list;
	}
	
//bytesable				

	public static <T extends Bytesable> byte[] enbyteBytesable(T data)
	{
		if(data == null)
			return enbyteBytes(null);
		return enbyteBytes(data.toBytes());
	}
	
	public static <T extends Bytesable> T debyteBytesable(Class<T> clazz, byte[] data)
	{
		if(data == null)
		{
			throw new DebyteException("[debyteBytesable]Input bytes can not be null.");
		}
		byte[] bs = debyteBytes(data);
		return recoverBytesable(clazz, bs);
	} 
	
	public static <T extends Bytesable> T recoverBytesable(Class<T> clazz, byte[] bs)
	{
		if(clazz == null)
		{
			return null;
		}
		if(bs == null)
		{
			return null;
		}
		try
		{		
			Constructor<T> cst = clazz.getConstructor(null);
			return (T)cst.newInstance(null).initFromBytes(bs);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new DebyteException("[debyteBytesable]exception:"+ e.getMessage());
		}		
	} 

	public static <T extends Bytesable> byte[] enbyteList(List<T> list, boolean withLen)
	{
		if(list == null)
		{
			return withLen ? enbyteBytes(null) : null;
		}
		if(list.size() == 0)
		{
			return withLen ? enbyteBytes(new byte[]{}) : new byte[]{};
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for(T b : list)
		{
			try
			{
				bos.write(enbyteBytes(b.toBytes()));
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return withLen ? enbyteBytes(bos.toByteArray()) : bos.toByteArray();		
	}
	
	public static <T extends Bytesable> byte[] enbyteList(List<T> list)
	{
		return enbyteList(list, true);
	}
	
	public static <T extends Bytesable> List<T> debyteList(Class<T> clazz, byte[] data, boolean withLen) throws DebyteException
	{
		if(data == null)
		{
			throw new DebyteException("[debyteList]Input bytes can not be null.");
		}
		return recoverList(clazz, withLen ? debyteBytes(data) : data);
	}
	public static <T extends Bytesable> List<T> debyteList(Class<T> clazz, byte[] data) throws DebyteException
	{
		return debyteList(clazz, data, true);
	}
	
	public static <T extends Bytesable> List<T> recoverList(Class<T> clazz, byte[] bs) throws DebyteException
	{
		List<T> tlist = null;	
		try
		{
			tlist = new ArrayList<T>();
			List<byte[]> list = recoverBytesList(bs);
			if(list == null)
			{
				return null;
			}
			for (byte[] bb : list)
			{
				tlist.add(recoverBytesable(clazz, bb));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new DebyteException("[debyteList]exception:"+ e.getMessage());
		}
		return tlist;
	}	
	
	private static <T> byte[] enbyteList(int type, List<T> array, boolean withLen)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for(T b : array)
		{
			try
			{
				switch(type)
				{
					case Bytesable.INTEGER:
						bos.write(enbyteInt((Integer)b));
						break;
					case Bytesable.SHORT:
						bos.write(enbyteShort((Short)b));
						break;
					case Bytesable.LONG:
						bos.write(enbyteLong((Long)b));
						break;
					case Bytesable.BOOLEAN:
						bos.write(enbyteBoolean((Boolean)b));
						break;
				}
			} 
			catch (IOException e)
			{
				throw new EnbyteException("[enbyte]IOException .");
			}
		}
		return withLen ? enbyteBytes(bos.toByteArray()) : bos.toByteArray();
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
		System.out.println(ByteUtil.debyteIntList(ByteUtil.enbyteIntList(list1, false), false));
		System.out.println(ByteUtil.debyteIntList(ByteUtil.enbyteIntList(list1, true), true));
		//short
		System.out.println("------test short-------");
		ArrayList<Short> list2 = new ArrayList<Short>();
		list2.add((short)1);
		list2.add((short)2);
		list2.add((short)3);
		System.out.println(ByteUtil.debyteShortList(ByteUtil.enbyteShortList(list2, false), false));
		System.out.println(ByteUtil.debyteShortList(ByteUtil.enbyteShortList(list2, true), true));
		//long
		System.out.println("------test long-------");
		ArrayList<Long> list3 = new ArrayList<Long>();
		list3.add(1L);
		list3.add(2L);
		list3.add(3L);
		System.out.println(ByteUtil.debyteLongList(ByteUtil.enbyteLongList(list3, false), false));
		System.out.println(ByteUtil.debyteLongList(ByteUtil.enbyteLongList(list3, true), true));		
		//boolean
		System.out.println("------test boolean-------");
		ArrayList<Boolean> list4 = new ArrayList<Boolean>();
		list4.add(true);
		list4.add(false);
		list4.add(true);
		System.out.println(ByteUtil.debyteBooleanList(ByteUtil.enbyteBooleanList(list4, false), false));
		System.out.println(ByteUtil.debyteBooleanList(ByteUtil.enbyteBooleanList(list4, true), true));		
		//string
		System.out.println("------test string-------");
		ArrayList<String> list5 = new ArrayList<String>();
		list5.add("a");
		list5.add("b");
		list5.add("c");
		System.out.println(ByteUtil.debyteStringList(ByteUtil.enbyteStringList(list5, false), false));
		System.out.println(ByteUtil.debyteStringList(ByteUtil.enbyteStringList(list5, true), true));		
		//bytes
		System.out.println("------test bytes-------");
		ArrayList<byte[]> list6 = new ArrayList<byte[]>();
		list6.add(new byte[]{1,2,3});
		list6.add(new byte[]{4,5,6});
		list6.add(new byte[]{7,8,9});
		System.out.println(ByteUtil.debyteBytesList(ByteUtil.enbyteBytesList(list6, false), false));
		System.out.println(ByteUtil.debyteBytesList(ByteUtil.enbyteBytesList(list6, true), true));		
		//bytesable
		System.out.println("------test bytesable-------");
		ArrayList<BitBuffer> list7 = new ArrayList<BitBuffer>();
		list7.add(new BitBuffer(new byte[]{1,1,1}));
		list7.add(new BitBuffer(new byte[]{2,2,2}));
		list7.add(new BitBuffer(new byte[]{3,3,3}));
		System.out.println(ByteUtil.debyteList(BitBuffer.class, ByteUtil.enbyteList(list7, false), false));
		System.out.println(ByteUtil.debyteList(BitBuffer.class, ByteUtil.enbyteList(list7, true), true));
	}
}
