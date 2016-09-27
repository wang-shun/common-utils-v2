package com.youzan.sz.jutil.net.protocol;

import com.youzan.sz.jutil.crypto.HexUtil;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compresser
{
	public static final int COMPRESS_BUFFER_SIZE = 256;
	public static final int DECOMPRESS_BUFFER_SIZE = 1024;
	public static byte[] compress(byte[] bs)
	{
		if(bs == null || bs.length == 0) return bs;
		//System.out.println("[compress]org len:"+ bs.length);	
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		Deflater compresser = new Deflater();
		compresser.setInput(bs);
		compresser.finish();
		byte[] compressData = new byte[COMPRESS_BUFFER_SIZE];
		while(!compresser.finished())
		{
			int compressLen = compresser.deflate(compressData);
			bao.write(compressData, 0, compressLen);
		}	
		compresser.finish();
		byte[] result = bao.toByteArray();
		//System.out.println("compress len:"+ result.length);
		return result;
	}
	
	public static byte[] deCompress(byte[] bs) throws DataFormatException
	{
		if(bs == null || bs.length == 0) return bs;		
		//System.out.println("[deCompress]org len:"+ bs.length);	
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		Inflater decompresser = new Inflater();
		decompresser.setInput(bs);
		byte[] decompressData = new byte[DECOMPRESS_BUFFER_SIZE];
		while(!decompresser.finished())
		{
			int resultLength = decompresser.inflate(decompressData);
			bao.write(decompressData, 0, resultLength);
		}
		decompresser.end();
		byte[] result = bao.toByteArray();
		//System.out.println("[deCompress]decompress len:"+ result.length);
		return result;
	}
	
	public static void main(String[] args) throws DataFormatException
	{
		byte[] bs = "http://photo.store.qq.com/http_imgload.cgi?/rurl2=f284645a2c0082e5f18d4a0b705d039028c489a0ef9cc7ac94434d0e233d0876d9aedb26b2bc9f499a03d95d1b4d15e311e98f7f99900195932f8db7eb6454082ec7aee3f53952f9fa70067b6adf94ce2b901c9a".getBytes();
		System.out.println(HexUtil.bytes2HexStr(bs));
		System.out.println(bs.length);
/*		long tt = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++)
		{
			compress(bs);
		}
		System.out.println(System.currentTimeMillis() - tt);*/
		System.out.println(new String(compress(bs)));
	}
}
