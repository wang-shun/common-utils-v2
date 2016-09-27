package com.youzan.sz.jutil.net.protocol;

import com.youzan.sz.jutil.bytes.ByteUtil;
import com.youzan.sz.jutil.crypto.HexUtil;
import com.youzan.sz.jutil.j4log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//import com.qq.jutil.bytes.ByteUtil;
//import com.qq.jutil.j4log.Logger;

public class NetPackage
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	public static final byte FLAG_COMPRESS = 0x01;//是否压缩0000 0001

	private static final int MIN_COMPRESS_SIZE = 128;
	
	private byte flag;
	private byte[] data;

	public NetPackage(byte[] data)
	{
		this.flag = 0;		
		this.data = data;
	}
	
	private NetPackage(byte flag, byte[] data)
	{
		this.flag = flag;		
		this.data = data;
	}	

	public static void sendNetPackage(OutputStream out, NetPackage pkg, boolean compress)
	{
		try
		{
			out.write(pkg.toNetPackage(compress));
		}
		catch (IOException e)
		{
			throw new NetException("sendNetPackage exception:"+ e.getMessage());
		}		
	}
	
	public static void sendNetPackage(OutputStream out, NetPackage pkg)
	{
		try
		{
			out.write(pkg.toNetPackage(false));
		}
		catch (IOException e)
		{
			throw new NetException("sendNetPackage exception:"+ e.getMessage());
		}
	}
	
	public static NetPackage readNetPackage(InputStream in,int maxPkgSize)
	{
		int len = -1;
		int step = 0;
		try
		{
			//包结构pkgLen|flag|data
			//取包长
			byte[] bsLen = new byte[4];
			in.read(bsLen);
			step = 1;
			len = ByteUtil.debyteInt(bsLen);
			if(len > maxPkgSize)
			{
				logger.error("[NetPackage]readNetPackage len="+len+"|maxPkgSize="+maxPkgSize);
				throw new RuntimeException("[NetPackage]readNetPackage len="+len+"|maxPkgSize="+maxPkgSize);
			}
			step = 2;
			//System.out.println("readNetPackage,len:"+ len);
			//取flag
			byte flag = (byte)in.read();
			step = 3;
			//读包数据
			int toReadLen = len - 1;
			byte[] data = new byte[toReadLen];
			step = 4;
			int readedLen = 0;
			while(readedLen < toReadLen)
			{
				int bytesRead = in.read(data, readedLen, toReadLen - readedLen);
				if(bytesRead < 0)
					throw new IOException("close by peer."); 
				readedLen += bytesRead;
			}
			step = 5;
			//解压
			if(isCompressed(flag))
			{//数据经过压缩
				data = Compresser.deCompress(data);
			}
			step = 6;
			return new NetPackage(flag, data);
		}
		catch(Throwable e)
		{
			//e.printStackTrace();
			throw new NetException("readNetPackage exception len:"+len+"|step:"+step, e);
		}		
	}
	
	public static NetPackage readNetPackage(InputStream in)
	{
		//默认不作限制
		return readNetPackage(in, Integer.MAX_VALUE);
	}
	
	public byte[] toNetPackage(boolean needCompress)
	{
		if(this.data == null || this.data.length == 0)
		{
			throw new NetException("NetPackage data is null or empty.");
		}
			
		//包结构pkgLen|flag|data
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			byte[] sendData = this.data;
			boolean toCompress = false;
			if(needCompress && (this.data != null && this.data.length >= MIN_COMPRESS_SIZE))
			{
				setCompressed(true);
				toCompress = true;
			}
			else
			{
				setCompressed(false);
				toCompress = false;				
			}
			//写数据
			if(toCompress)
			{//数据需要压缩
				sendData = Compresser.compress(this.data);	
			}
			int pkgLen = 1 + sendData.length;//flag + dataLen
			//System.out.println("toNetPackage,len:"+ pkgLen);
			bos.write(ByteUtil.enbyteInt(pkgLen));//写pkgLen
			bos.write(this.flag);//写标志位
			bos.write(sendData);//写数据
			//System.out.println("real len:"+ bos.toByteArray().length);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new NetException("toNetPackage exception:"+ e.getMessage());
		}
		return bos.toByteArray();
	}
	
	private void setCompressed(boolean isCompress)
	{
		if(isCompress)
			this.flag |= FLAG_COMPRESS;
		else
			this.flag &= (~FLAG_COMPRESS);
	}	
	public boolean isCompress()
	{
		return isCompressed(this.flag);
	}
	
	public static boolean isCompressed(byte flag)
	{
		return (flag & FLAG_COMPRESS) != 0;
	}	

	public byte[] getData()
	{
		return data;
	}

	public void setData(byte[] data)
	{
		this.data = data;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[NetPackage: ");
		sb.append(flag).append(", ");
		sb.append(data == null ? "null" : HexUtil.bytes2HexStr(data));
		sb.append("]");
		return sb.toString();
	}
/*	public static void main(String[] args)
	{
		String str = "adsfkjal sdioqwyer90 `	u23hyko i234yhlkhjfiq3er kafjqhwefaklsdjfhio2345r234y5902834hasdkljfhasdhfasjdfasdjfa;sjdfasjdopfuq23oprjl;asdfjasdfbckv ne wrt23t216+ 4f";
		byte[] bs = str.getBytes();
		System.out.println(bs.length);
		NetPackage pkg = new NetPackage(str.getBytes());
		ByteArrayInputStream bai = new ByteArrayInputStream(pkg.toNetPackage(true));
		System.out.println(pkg.toNetPackage(true).length);
		System.out.println(new String(NetPackage.readNetPackage(bai).data));
	}*/
}