package com.youzan.sz.jutil.jcache.adv;

import java.io.Serializable;

final class NodeForPersistent<K, V> implements Serializable
{
	private static final long serialVersionUID = 5158911174863010224L;
	
	K	key;			// Key
	V	value;			// Value
	long	timeDiff;	// 时间差
	
	public NodeForPersistent(Node<K, V> n, long now)
	{
		key = n.key;
		value = n.value;
		timeDiff = now - n.createTime;
		if(timeDiff < 0)
			timeDiff = 0;
	}
	
	public static <KT, VT> NodeForPersistent<KT, VT> make(Node<KT, VT> n, long now)
	{
		return new NodeForPersistent<KT, VT>(n, now);
	}
}
