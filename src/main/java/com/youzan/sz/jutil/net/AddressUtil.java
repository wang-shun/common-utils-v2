package com.youzan.sz.jutil.net;

import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Pair;

import java.net.InetAddress;

//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Pair;

public final class AddressUtil
{
	public static Pair<String, Integer> parseAddress(String addr)
	{
		if(addr == null)
			return null;
		String[] s = StringUtil.split(addr, ":");
		if(s.length != 2 || s[0].length() == 0)
			return null;
		int port = StringUtil.convertInt(s[1], -1);
		if(port <= 0)
			return null;
		return Pair.makePair(s[0], port);
	}
	
	public static Pair<String, Integer> parseAddress(String addr, String defaultAddr)
	{
		Pair<String, Integer> pr = parseAddress(addr);
		return pr != null ? pr : parseAddress(defaultAddr);
	}
	
	/**
	 * 将IP地址转换成InetAddress，系统底层不会产生域名服务调用
	 * @param rawIP IP地址的串，比如"172.18.16.36"
	 * @return null if rawIP error
	 */
	public static InetAddress ip2Addr(String rawIP)
	{
		byte[] addr = textToNumericFormatV4(rawIP);
		try
		{
			InetAddress address = InetAddress.getByAddress(rawIP, addr);
			return address;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

    public static byte[] textToNumericFormatV4(String paramString) {
        if (paramString.length() == 0) {
            return null;
        }

        byte[] arrayOfByte = new byte[4];
        //String[] arrayOfString = paramString.split("\\.", -1);
        String[] arrayOfString = StringUtil.split(paramString, ".");
        try {
            long l;
            int i;
            switch (arrayOfString.length) {
            case 1:
                l = Long.parseLong(arrayOfString[0]);
                if ((l < 0L) || (l > 4294967295L))
                    return null;
                arrayOfByte[0] = (byte) (int) (l >> 24 & 0xFF);
                arrayOfByte[1] = (byte) (int) ((l & 0xFFFFFF) >> 16 & 0xFF);
                arrayOfByte[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                arrayOfByte[3] = (byte) (int) (l & 0xFF);
                break;
            case 2:
                l = Integer.parseInt(arrayOfString[0]);
                if ((l < 0L) || (l > 255L))
                    return null;
                arrayOfByte[0] = (byte) (int) (l & 0xFF);
                l = Integer.parseInt(arrayOfString[1]);
                if ((l < 0L) || (l > 16777215L))
                    return null;
                arrayOfByte[1] = (byte) (int) (l >> 16 & 0xFF);
                arrayOfByte[2] = (byte) (int) ((l & 0xFFFF) >> 8 & 0xFF);
                arrayOfByte[3] = (byte) (int) (l & 0xFF);
                break;
            case 3:
                for (i = 0; i < 2; ++i) {
                    l = Integer.parseInt(arrayOfString[i]);
                    if ((l < 0L) || (l > 255L))
                        return null;
                    arrayOfByte[i] = (byte) (int) (l & 0xFF);
                }
                l = Integer.parseInt(arrayOfString[2]);
                if ((l < 0L) || (l > 65535L))
                    return null;
                arrayOfByte[2] = (byte) (int) (l >> 8 & 0xFF);
                arrayOfByte[3] = (byte) (int) (l & 0xFF);
                break;
            case 4:
                for (i = 0; i < 4; ++i) {
                    l = Integer.parseInt(arrayOfString[i]);
                    if ((l < 0L) || (l > 255L))
                        return null;
                    arrayOfByte[i] = (byte) (int) (l & 0xFF);
                }
                break;
            default:
                return null;
            }
        } catch (NumberFormatException localNumberFormatException) {
            return null;
        }
        return arrayOfByte;
    }

    public static long textToLongFormatV4(String paramString) {
        byte[] ip = textToNumericFormatV4(paramString);
        if(ip == null)
        {
            return -1;
        }
        else
        {
            return ((ip[0] << 24) & 0xFFFFFFFFL) + ((ip[1] << 16) & 0xFFFFFF) + ((ip[2] << 8) & 0xFFFF) + (ip[3] & 0xFF);
        }
    }
    
    public static boolean isIPv4LiteralAddress(String paramString) {
        return (textToNumericFormatV4(paramString) != null);
    }

	public static void main(String[] args) {
		System.out.println(ip2Addr("172.18.16.86"));
		System.out.println(textToLongFormatV4("219.133.62.73"));
	}
}
