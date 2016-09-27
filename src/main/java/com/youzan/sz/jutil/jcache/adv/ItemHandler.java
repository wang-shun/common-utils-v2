package com.youzan.sz.jutil.jcache.adv;

/**
 * 超时、淘汰节点处理器
 * 当cache检测到一个节点超时，或因为长时间没有访问而被淘汰时，会调用该接口进行处理
 * @author meteorchen
 *
 * @param <K>
 * @param <V>
 */
public interface ItemHandler<K, V>
{
	/**
	 * 处理一个元素，当AdvCache清除元素之前，会调用该方法做一些处理
	 * @param n				要清除的元素节点
	 * @param isTimeout		是否因为超时而清除（要么是超时清除，要么是淘汰清除）
	 * @return				是否需要从缓存中删除该元素
	 */
	boolean handle(Node<K, V> n, boolean isTimeout);
}
