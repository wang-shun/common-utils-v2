package com.youzan.sz.jutil.net.protocol;

import com.youzan.sz.jutil.bytes.ByteUtil;
import com.youzan.sz.jutil.bytes.Bytesable;
import com.youzan.sz.jutil.bytes.Debyter;
import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

//import com.qq.jutil.bytes.ByteUtil;
//import com.qq.jutil.bytes.Bytesable;
//import com.qq.jutil.bytes.Debyter;

public class Request
{
	public static final byte FLAG_NEED_RETURE = 1<<0;//是否需要返回值0000 0001
	public static final byte FLAG_COMPRESS = 1<<1;//是否压缩0000 0010
	public static final byte FLAG_ASSEMBLE = 1<<2;//是否组包数据0000 0100
	public static final byte FLAG_RESEND = 1<<3;//是否重发0000 1000
	
	private int cmdId;
	private byte flag;
	private byte[] data;
		
	public Request(int cmdId, byte[] data)
	{
		this(cmdId,(byte)0, data);
	}
	
	private Request(int cmdId, byte flag, byte[] data)
	{
		this.cmdId = cmdId;
		this.flag = flag;
		this.data = data;
	}
	
	public static Request recoverRequest(byte[] bytes) throws DataFormatException
	{
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		int cmdId = Debyter.getInt(bb);
		byte flag = bb.get();
		byte[] data = Debyter.getBytes(bb);
		return new Request(cmdId, flag, data);
	}
	
	public byte[] toBytes()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			bos.write(ByteUtil.enbyteInt(cmdId));
			bos.write(flag);
			bos.write(ByteUtil.enbyteBytes(data));
		}
		catch(IOException e)
		{
			throw new RuntimeException("ToBytes fail."+ e.getMessage());
		}
		return bos.toByteArray();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[Request: ");
		sb.append(cmdId).append(", ");
		sb.append(flag).append(", ");
		sb.append(data == null ? "null" : HexUtil.bytes2HexStr(data));
		sb.append("]");
		return sb.toString();
	}
	public int getCmdId()
	{
		return cmdId;
	}
	public void setCmdId(int cmdId)
	{
		this.cmdId = cmdId;
	}
	public byte[] getData()
	{
		return data;
	}
	public void setData(byte[] data)
	{
		this.data = data;
	}
	
	public <T extends Bytesable> T getParameter(Class<T> clazz)
	{
		if(isAssemble())
		{//请求是组包的,不能直接取参数
			throw new NetException("Parameter is assemble,please use getParameters() to get parameters.");
		}
		return ByteUtil.debyteBytesable(clazz, this.data);
	}
	
	public <T extends Bytesable> List<T> getParameters(Class<T> clazz)
	{
		if(!isAssemble())
		{//请求是单包的
			throw new NetException("Parameter is not assemble,please use getParameter() to get parameter.");
		}
		return ByteUtil.debyteList(clazz, this.data);
	}

	public byte getFlag()
	{
		return flag;
	}
	public void setFlag(byte flag)
	{
		this.flag = flag;
	}
	
	public boolean isNeedReturn()
	{
		return (this.flag & FLAG_NEED_RETURE) != 0;
	}
	
	public void setFlagBit(byte position, boolean value)
	{
		if(value)
			this.flag |= position;
		else
			this.flag &= (~position);
	}
	
	public void setNeedReturn(boolean needReturn)
	{
		setFlagBit(FLAG_NEED_RETURE, needReturn);
	}
	
	public boolean isCompress()
	{
		return (this.flag & FLAG_COMPRESS) != 0;
	}
	
	public void setCompress(boolean isCompress)
	{
		setFlagBit(FLAG_COMPRESS, isCompress);
	}	
	
	public boolean isAssemble()
	{
		return (this.flag & FLAG_ASSEMBLE) != 0;
	}
	
	public void setAssemble(boolean isAssemble)
	{
		setFlagBit(FLAG_ASSEMBLE, isAssemble);
	}	
	
	public boolean isResend()
	{
		return (this.flag & FLAG_RESEND) != 0;
	}
	
	public void setResend(boolean isResend)
	{
		setFlagBit(FLAG_RESEND, isResend);
	}		
}