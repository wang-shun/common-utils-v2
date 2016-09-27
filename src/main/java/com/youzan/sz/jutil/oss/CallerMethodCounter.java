package com.youzan.sz.jutil.oss;

import com.youzan.sz.jutil.util.Pair;

import java.util.List;
//import com.qq.jutil.util.Pair;

/**
 * 调用者的计数器
 * @author junehuang
 */
public interface CallerMethodCounter {
	/**
	 * 返回每个方法的计数
	 * @return
	 */
	public List<Pair<String,Counter>> getOssAllMethodCounter();
	
	/**
	 * 获取一个方法的计数
	 * @param method
	 * @return
	 */
	public Counter getOssOneMethodCounter(String method);
}
