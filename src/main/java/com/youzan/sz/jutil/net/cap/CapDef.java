package com.youzan.sz.jutil.net.cap;
/**
 * cap协议声明，包括size字段，seq字段，body的偏移量和size和seq的字节数
 * @author junehuang
 *
 */
public class CapDef {
	public final int SIZE_OFFSET;
	public final int SIZE_LEN;
	public final int SEQ_OFFSET;
	public final int SEQ_LEN;

	//注释是simple协议的值
//	SIZE_OFFSET=0;
//	SIZE_LEN=4;
//	SEQ_OFFSET=4;
//	SEQ_LEN=4;
	
	public CapDef(int sizeOffset,int sizeLen,int seqOffset,int seqLen){
		SIZE_OFFSET = sizeOffset;
		SIZE_LEN = sizeLen;
		SEQ_OFFSET = seqOffset;
		SEQ_LEN = seqLen;
		
		if( SIZE_LEN!=4 && SIZE_LEN!=2 )
			throw new RuntimeException("SIZE_LEN must be 2 or 4");
		
		if( SEQ_LEN!=4 && SEQ_LEN!=2 )
			throw new RuntimeException("SEQ_LEN must be 2 or 4");

		if( SIZE_OFFSET<0 || SEQ_OFFSET<0 )
			throw new RuntimeException("OFFSET must greater than 0");
	}
}
