package com.youzan.sz.jutil.oss;

/**
 * 染色枚举类型定义
 * @author junehuang
 *
 */
public enum ColorType {
	UIN(0),USER_IP(1),UA(2),QUA(3);
	
	private static ColorType[] array = {UIN,USER_IP,UA,QUA};
	
	private final int value;
	
	private ColorType(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public static ColorType valueOf( int value ){
		if( value < 0 || value >= array.length )
			return null;
		return array[value];
	}
}
