package com.youzan.sz.jutil.oss;

import com.youzan.sz.jutil.util.Pair;

import java.util.List;

//import com.qq.jutil.util.Pair;
/**
 * 被调用者的计数
 * @author junehuang
 */
public interface CalleeIpMethodCounter {
	/**
	 * 返回每个IP-方法对的计数
	 * @return
	 */
	public List< Pair<Pair<String,String>, Counter > > getOssAllIpMethodCounter();
	
	/**
	 * 获取一个IP-方法对方法的计数
	 * @param method
	 * @return
	 */
	public Counter getOssOneIpMethodCounter(Pair<String, String> ipMethod);
}
