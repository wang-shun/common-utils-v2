package com.youzan.sz.jutil.high_available.monitor;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.thread.FixThreadExecutor;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.thread.FixThreadExecutor;

public final class MonitorFacotry
{
	private static final Logger                             logger  = Logger.getLogger("jutil");
	private static final ConcurrentHashMap<String, Monitor> m       = new ConcurrentHashMap<String, Monitor>();
	private static final FixThreadExecutor                  tpAlive = new FixThreadExecutor(4, 10000);
	private static final FixThreadExecutor                  tpDead  = new FixThreadExecutor(1, 10000);
	
	private static final Thread t = new Thread(new Runnable(){
		public void run()
		{
			while(true){
				try{
					Enumeration<Monitor> e = m.elements();
					while(e.hasMoreElements()){
						final Monitor mon = e.nextElement();
						if(!mon.timeToCheck())
							continue;
						if(mon.getState() == Monitorable.STATE_ALIVE){
							tpAlive.execute(new Runnable(){
								public void run()
								{
									mon.checkAlive();
								}
							});
						}else{
							tpDead.execute(new Runnable(){
								public void run()
								{
									mon.checkDead();
								}
							});
						}						
					}
					Thread.sleep(3000);
				}catch(Throwable e){
					logger.error("", e);
				}
			}
		}
	});
	
	static
	{
		t.start();
	}
	
	public static Monitor getMonitor(String name)
	{
		return m.get(name);
	}
	
	public static Monitor createMonitor(String name, Monitorable ma, long checkInterval)
	{
		Monitor mon = m.get(name);
		if(mon != null)
			return mon;
		mon = new Monitor(name, ma, checkInterval);
		m.put(name, mon);
		return mon;
	}
}
