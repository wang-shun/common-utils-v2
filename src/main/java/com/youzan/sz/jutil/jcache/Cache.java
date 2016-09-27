package com.youzan.sz.jutil.jcache;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Cache的接口
 * @author meteor
 *
 * @param <K>	key的类型
 * @param <V>	value的类型
 */
public interface Cache<K, V>
{
	/**
	 * 获取key对应的cache信息
	 * @param key	key
	 * @return V
	 */
	V get(K key);
	
	/**
	 * 判断key是否在cache中存在
	 * @param key	key
	 * @return boolean
	 */
	boolean containsCache(K key);
	
	/**
	 * 把数据加入cache中
	 * @param key		key
	 * @param value		value
	 * @return V	cache中本来已经存在的数据，若无，则返回null
	 */
	V put(K key, V value);
	
	/**
	 * 清除所有cache
	 *
	 */
	void clear();
	
	/**
	 * 删除指定key对应的cache
	 * @param key	key
	 * @return V
	 */
	V remove(K key);
	
	/**
	 * 获取当前cache的大小
	 * @return int 
	 */
	int size();
	
	/**
	 * 执行清除操作
	 * @return int 
	 */
	int cleanUp();
	
	/**
	 * 获得所有的key
	 * @return Set<K>
	 */
	Set<K> keySet();
	
	/**
	 * 统计接口，获取查询次数
	 * @return int 
	 */
	int getQueryCount();
	
	/**
	 * 统计接口，获取命中次数
	 * @return int 
	 */
	int getHitCount();
	
	/**
	 * 统计接口，获取该cache占用的总内存大小
	 * @return	若支持，则返回内存大小，否则返回-1
	 */
	int getMemoryUsage();
	
	/**
	 * 返回一个迭代器，用于遍历整个cache
	 * @return	该迭代器的cache
	 */
	Iterator<Map.Entry<K, V>> iterator();
}
