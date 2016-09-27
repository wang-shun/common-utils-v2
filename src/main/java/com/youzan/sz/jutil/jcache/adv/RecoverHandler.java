package com.youzan.sz.jutil.jcache.adv;

/**
 * 从磁盘恢复时的回调接口
 * @author meteorchen
 *
 * @param <K>
 * @param <V>
 */
public interface RecoverHandler<K, V>
{
	/**
	 * 当从磁盘恢复数据时，每读取到一个节点，先调用该接口
	 * @param n		读取到的节点数据
	 * @return		如果返回true，则表示该节点需要添加到cache里，否则，直接丢弃该节点
	 */
	boolean handle(Node<K, V> n);
}
