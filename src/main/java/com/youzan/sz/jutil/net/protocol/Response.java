package com.youzan.sz.jutil.net.protocol;

import com.youzan.sz.jutil.bytes.ByteUtil;
import com.youzan.sz.jutil.bytes.Bytesable;
import com.youzan.sz.jutil.bytes.Debyter;
import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
//
//import com.qq.jutil.bytes.ByteUtil;
//import com.qq.jutil.bytes.Bytesable;
//import com.qq.jutil.bytes.Debyter;


public class Response
{	
	public static final int CODE_OK = 200;
	public static final int CODE_NO_HANDLER = 404;
	public static final int CODE_NO_REQUEST = 405;
	public static final int CODE_SERVER_ERROR = 500;
	public static final int CODE_EXCEPTION = 501;
	public static final int CODE_CLIENT_EXCEPTION = 502;
	public static final int CODE_INVALID_SOCKET = 503;
	//-999 ~ 1000是系统保留的返回码,自定义的返回码请使用些范围外的数据值
	
	private int code;
	private byte[] data;

	public Response(int code, byte[] data)
	{
		this.code = code;
		this.data = data;
	}
	
	public static Response recoverResponse(byte[] bytes) throws DataFormatException
	{
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		int code = Debyter.getInt(bb);
		byte[] data = Debyter.getBytes(bb);
		return new Response(code, data);
	}
	
	public byte[] toBytes()
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			bos.write(ByteUtil.enbyteInt(code));
			bos.write(ByteUtil.enbyteBytes(data));
		}
		catch(IOException e)
		{
			throw new RuntimeException("ToBytes fail."+ e.getMessage());
		}
		//System.out.println("init:"+ HexUtil.bytes2HexStr(bos.toByteArray()));
		return bos.toByteArray();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[Response: ");
		sb.append(code).append(", ");
		sb.append(data == null ? "null" : HexUtil.bytes2HexStr(data));
		sb.append("]");
		return sb.toString();
	}

	public int getCode()
	{
		return code;
	}

	public void setCode(int code)
	{
		this.code = code;
	}

	public byte[] getData()
	{
		return data;
	}
	
	public <T extends Bytesable> T getObject(Class<T> clazz)
	{
		return ByteUtil.debyteBytesable(clazz, this.data);
	}
	
	public void setData(byte[] data)
	{
		this.data = data;
	}

	public String getErrorMsg()
	{
		if(this.code == CODE_OK || this.data == null)
		{
			return "";
		}
		else
		{
			return ByteUtil.debyteString(data);
		}
	}
}