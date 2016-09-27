package com.youzan.sz.jutil.jcache;

import java.io.Serializable;

@SuppressWarnings("serial")
final class CacheValue<V> implements Serializable
{
	long time;
	V value;
	int memoryUsage;
}
