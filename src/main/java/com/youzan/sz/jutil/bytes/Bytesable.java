package com.youzan.sz.jutil.bytes;

import java.io.Serializable;

public interface Bytesable extends Serializable
{
	public static final int INTEGER = 4;
	public static final int SHORT = 2;
	public static final int LONG = 8;
	public static final int BOOLEAN = 1;
	public static final int BYTES = -1;	
	public static final int STRING = -2;
	public static final int BYTESABLE = -3;
	public static final int INT_LIST = -4;
	public static final int LONG_LIST = -5;
	public static final int BOOLEAN_LIST = -6;
	public static final int BYTES_LIST = -7;
	public static final int STRING_LIST = -8;
	public static final int LIST = -9;
	
	public Bytesable initFromBytes(byte[] bs);
	public byte[] toBytes();
}
