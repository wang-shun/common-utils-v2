/**
 * 
 */
package com.youzan.sz.jutil.jcache;

//import com.qq.jutil.jcache.monitor.Visitable;

import com.youzan.sz.jutil.jcache.monitor.Visitable;

/**
 * @author bingyi
 *
 */
public abstract class AutoRefreshCacheBase<K, V> implements Cache<K, V>,Visitable {

    abstract public long getReflushTime();
    
    public abstract String getReflushingStatus();

}
