package com.youzan.sz.jutil.jcache.monitor;
//
//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.CacheFactory;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.CacheFactory;

public class DefaultCacheMonitor implements CacheMonitor {

	private MonitorVisitor monitorVisitor = new MonitorVisitor();

	private static final Logger logger = Logger.getLogger("jutil");

	private Thread monitorThread;

	private static final int monitorInterval = 5 * 60 * 1000;// 5min

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.qq.jutil.jcache.monitor.CacheMonitor#log()
	 */
	public void log() {
		String[] names = CacheFactory.getAllCacheName();
		for (String name : names) {
			Cache<?, ?> c = CacheFactory.getCache(name);
			if (c instanceof Visitable) {
				((Visitable) c).accept(monitorVisitor, name);
			} else if (c instanceof Cache<?, ?>) {
				monitorVisitor.visitCustomCache(c, name);
			}
		}
	}

	public void start() {
		monitorThread = new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(monitorInterval);
						log();
					} catch (Exception e) {
						logger.error("DefaultCacheMonitor run error: ", e);
					}
				}
			}
		};
		try {
			monitorThread.start();
		} catch (Exception e) {
			logger.error("DefaultCacheMonitor monitor thread start error: ", e);
		}
	}

}
