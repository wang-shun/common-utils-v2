package com.youzan.sz.jutil.jcache.monitor;
//
//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.AutoRefreshCache;
//import com.qq.jutil.jcache.AutoRefreshCacheBase;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.adv.AdvAutoSaveCache;
//import com.qq.jutil.jcache.adv.AdvCache;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.jcache.AutoRefreshCacheBase;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.adv.AdvAutoSaveCache;
import com.youzan.sz.jutil.jcache.adv.AdvCache;

/**
 * 
 * jcache监控的日志格式，如果该项没有则以 - 来显示：
 * 
 * jmx端口 \t cache名字 \t cache实现类 \t size \t 命中次数 \t 查询次数 \t 刷新时间
 * 
 */
public class MonitorVisitor implements Visitor {

	private static final Logger logger = Logger.getLogger("jcacheMonitor");

	private static final String jmsPort = System.getProperty("com.sun.management.jmxremote.port", "-");

	@Override
	public void visitAdvAutoSaveCache(AdvAutoSaveCache<?, ?> cache, String name) {
		int hitCount = cache.getHitCount();
		int size = cache.size();
		int queryCount = cache.getQueryCount();
		long reflushTime = cache.getReflushTime();
		logger.info(jmsPort + "\t" + name + "\t" + cache.getClass().getName() + "\t" + size + "\t" + hitCount + "\t" + queryCount + "\t" + reflushTime);
	}

	@Override
	public void visitAdvCache(AdvCache<?, ?> cache, String name) {
		int hitCount = cache.getHitCount();
		int size = cache.size();
		int queryCount = cache.getQueryCount();
		long reflushTime = cache.getReflushTime();
		logger.info(jmsPort + "\t" + name + "\t" + cache.getClass().getName() + "\t" + size + "\t" + hitCount + "\t" + queryCount + "\t" + reflushTime);
	}

	@Override
	public void visitAutoRefreshCache(AutoRefreshCacheBase<?, ?> cache, String name) {
		int hitCount = cache.getHitCount();
		int size = cache.size();
		int queryCount = cache.getQueryCount();
		long reflushTime = cache.getReflushTime();
		logger.info(jmsPort + "\t" + name + "\t" + cache.getClass().getName() + "\t" + size + "\t" + hitCount + "\t" + queryCount + "\t" + reflushTime);
	}

	@Override
	public void visitCustomCache(Cache<?, ?> cache, String name) {
		int hitCount = cache.getHitCount();
		int size = cache.size();
		int queryCount = cache.getQueryCount();
		logger.info(jmsPort + "\t" + name + "\t" + cache.getClass().getName() + "\t" + size + "\t" + hitCount + "\t" + queryCount + "\t-");
	}

}
