package com.youzan.sz.jutil.jcache.monitor;

//import com.qq.jutil.jcache.AutoRefreshCacheBase;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.adv.AdvAutoSaveCache;
//import com.qq.jutil.jcache.adv.AdvCache;

import com.youzan.sz.jutil.jcache.AutoRefreshCacheBase;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.adv.AdvAutoSaveCache;
import com.youzan.sz.jutil.jcache.adv.AdvCache;

public interface Visitor {

	void visitAutoRefreshCache(AutoRefreshCacheBase<?, ?> cache, String name);

	void visitAdvAutoSaveCache(AdvAutoSaveCache<?, ?> cache, String name);

	void visitAdvCache(AdvCache<?, ?> cache, String name);

	void visitCustomCache(Cache<?, ?> cache, String name);
}
