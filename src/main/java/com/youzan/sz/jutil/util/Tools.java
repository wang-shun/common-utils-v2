package com.youzan.sz.jutil.util;

import com.youzan.sz.jutil.string.StringUtil;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.qq.jutil.string.StringUtil;

public class Tools
{

	private static final long DEFAULT_UIN_VALUE = -1L;

	private static long validUinStart = DEFAULT_UIN_VALUE;

	private static long validUinEnd = DEFAULT_UIN_VALUE;

	/**
	 * 重新加载QQ号码的合法区间，仅供配置中心调用，其他业务不要调用，不然后果很严重。
	 * 
	 * @throws Throwable
	 * 
	 * @deprecated
	 */
	public static void reInitValidUinScope(String scopeStr) throws Throwable {
		// 先把设置之前的值获取出来
		long currentScopeStart = validUinStart;
		long currentScopeEndNow = validUinEnd;
		try {
			int index = scopeStr.indexOf(":");
			long scopeStart = Long.parseLong(scopeStr.substring(0, index).trim());
			long scopeEnd = Long.parseLong(scopeStr.substring(index + 1).trim());
			// 进行基本的判断
			if (scopeStart > 10000 && scopeEnd > 10000 && scopeEnd > scopeStart) {
				// 更新
				validUinStart = scopeStart;
				validUinEnd = scopeEnd;
			} else {
				// 抛异常
				throw new IllegalArgumentException("com.qq.jutil.util.Tools.reInitValidUinScope(" + scopeStr + ") error!");
			}
		} catch (Throwable e) {
			// 有异常则设回之前的值
			validUinStart = currentScopeStart;
			validUinEnd = currentScopeEndNow;
			// 把异常抛到上一层打log
			throw e;
		}
	}

    /**
     * 支持变量文件名，例如${logRoot}/abc
     * 
     * @param path
     * @return
     */
    public static String getFilePath(String path)
    {
        if (path == null)
        {
            return "";
        }
        String r = "\\$\\{[^${}]*\\}";
        Pattern p = Pattern.compile(r);
        Matcher m = p.matcher(path);
        while (m.find())
        {
            int startIndex = m.start(); // index of start
            int endIndex = m.end(); // index of end + 1
            String currentMatch = path.substring(startIndex, endIndex);
            String property = System.getProperty(path.substring(startIndex + 2, endIndex - 1).trim());
            System.out.println(currentMatch + "=" + property);
            if (property == null)
            {
                System.err.println("System Property [" + path.substring(startIndex + 2, endIndex - 1).trim() + "] not found.");
                return "";
            }
            path = StringUtil.replaceAll(path, currentMatch, property);
            m = p.matcher(path);
        }
        return path;
    }

    /**
     * 将一个int型的uin号码转换成4字节的byte数组
     * 
     * @param value
     * @return
     */
    public static byte[] getUinBytes(int value)
    {
        byte[] data = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            data[3 - i] = (byte) (value >> 8 * i & 0xFF);
        }
        return data;
    }

    /**
     * 将一个long型值转换成4字节的byte数组
     * 
     * @param value
     * @return
     */
    public static byte[] getUinBytes(long value)
    {
        byte[] data = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            data[3 - i] = (byte) (value >> 8 * i & 0xFF);
        }
        return data;
    }

    /**
     * 传入一个8字节一下的byte数组，然后返回该数组对应的long型值(或者是一个无符号整形)
     * 
     * @param data
     * @return
     */
    public static long getUinLong(byte[] data)
    {
        if (null == data || data.length > 8)
        {
            throw new IllegalArgumentException("input data's length is error");
        }
        long result = 0;
        byte b;
        for (int i = 0; i < data.length; i++)
        {
            b = data[data.length - 1 - i];
            result += (long) (b & 0xFF) << 8 * i;
        }

        return result;

    }

    /**
     * 传入一个int值的uin，转换成对应的无符号的int值
     * 
     * @param value
     * @return
     */
    public static long getUinLong(int value)
    {
        return 0x00ffffffffl & value;
    }

    /**
     * 将传入的long形uin转换成int形uin，这个接口主要是用在调用外部接口时，外部接口没有更改成long行的临时方式
     * 
     * @param uins
     * @return
     */
    @Deprecated
    public static List<Integer> getUinsInt(List<Long> uins)
    {
        if (null == uins || uins.size() == 0)
        {
            return null;
        }
        List<Integer> us = new ArrayList<Integer>();
        for (Long l : uins)
        {
            us.add(l.intValue());
        }
        return us;
    }

    /**
     * 将传入的long形uin转换成int形uin，这个接口主要是用在调用外部接口时，外部接口没有更改成long行的临时方式
     * 
     * @param uins
     * @return
     */
    @Deprecated
    public static int[] getUinsInt(long[] uins)
    {
        if (null == uins || uins.length == 0)
        {
            return null;
        }
        int[] us = new int[uins.length];
        for (int i = 0; i < us.length; i++)
        {
            us[i] = (int) uins[i];
        }
        return us;
    }

    @Deprecated
    public static boolean isValidQQ(int uin)
    {
        return isValidQQ((long) uin);
    }

	/**
	 * 判断QQ号是否合法
	 * 
	 * @param uin
	 * @return boolean
	 */
	public static boolean isValidQQ(long uin) {
		// 先走代码判断，再走配置判断 ，以后QQ号扩容配置文件需要修改，jar包也要更新。
		if ((uin > 10000 && uin <= 4294967295L)) {
			return true;
		} else if (validUinStart != DEFAULT_UIN_VALUE && validUinEnd != DEFAULT_UIN_VALUE) {
			// 如果有配置则走配置判断一下
			return (uin >= validUinStart && uin <= validUinEnd);
		} else {
			return false;
		}
	}

    public static void main(String[] args) throws Throwable
    {
    	long uin = 4394967295l;
    	System.out.println(isValidQQ(uin));
    	reInitValidUinScope("10001:4394967295");
    	System.out.println(isValidQQ(uin));
    }

    public static MappedByteBuffer safetyMapping(FileChannel channel, MapMode mode, long position, long size) throws IOException
    {
        MappedByteBuffer buff = null;
        try
        {
            // System.out.println("[MappingTools]pos:"+ position +" size: "+
            // size);
            buff = channel.map(mode, position, size);
        }
        catch (Exception e)
        {
            System.out.println("File handle not free,gc and map again.");
            System.gc();// 强制释放file handle
            // 再map一次
            buff = channel.map(mode, position, size);
        }
        return buff;
    }
}
