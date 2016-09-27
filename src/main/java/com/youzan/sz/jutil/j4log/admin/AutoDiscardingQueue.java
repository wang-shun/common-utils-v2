package com.youzan.sz.jutil.j4log.admin;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * <b>功能：自动丢弃最老元素的queue</b><br>
 * <br>
 * <b>完整路径：</b> com.qq.jutil.j4log.admin.AutoDiscardingQueue <br>
 * <b>创建日期：</b> 2013-3-22 上午11:30:51 <br>
 * 
 * @author <a href="mailto:seonzhang@tencent.com">seonzhang(zhanggaojing)</a><br>
 *         <a href="http://www.tencent.com">Shenzhen Tencent Co.,Ltd.</a>
 * @version 1.0, 2013-3-22
 */
public class AutoDiscardingQueue<E> extends LinkedBlockingDeque<E>
{
	/** */
	private static final long serialVersionUID = -5538372602543388695L;

	public AutoDiscardingQueue()
	{
		super();
	}

	public AutoDiscardingQueue(int capacity)
	{
		super(capacity);
	}

	@Override
	public synchronized boolean offerFirst(E e)
	{
		if (remainingCapacity() == 0)
		{
			removeLast(); // 移除最 老元素
		}
		super.offerFirst(e);
		return true;
	}
}
