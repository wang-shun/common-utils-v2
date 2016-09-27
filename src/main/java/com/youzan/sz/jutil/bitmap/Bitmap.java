package com.youzan.sz.jutil.bitmap;

/**
 * Bitmap接口
 * @author sunnyin
 * @create 2009-9-14
 */
public interface Bitmap
{
	public static final int MAX_BIT_LEN = 8;
	public static final int MAX_EXT_SIZE = 8;

	/**
	 * 获取指定uin的bit值
	 * @param uin
	 * @return
	 * @deprecated
	 */
	public byte getBit(int uin);
	
	//新接口只支持boolean
	public boolean getBit(long uin);

	/**
	 * 设置指定uin的bit值
	 * @param uin
	 * @param value
	 * @deprecated
	 */
	public void setBit(int uin, byte value);
	
	//新接口只支持boolean
	public void setBit(long uin, boolean value);
	/**
	 * 获取扩展字段
	 * @return
	 */
	public byte[] getExtInfo();

	/**
	 * 设置扩展
	 * @param data
	 */
	public void setExtInfo(byte[] data);
	
	/**
	 * 清理bitmap，这个方法比较鸡肋
	 * @deprecated
	 * @return
	 */
	public void clear();
	
	/**
	 * 获取该Bitmap的capacity
	 * @return
	 */
	public long capacity();
	
}
