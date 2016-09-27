package com.youzan.sz.jutil.ha;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.util.Pair;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.util.Pair;

@SuppressWarnings("unchecked")
public class ObjectBalancer<E extends AliveChecker>
{
	private E[] objs;
	private Monitor[] mons;
	private boolean[] isAlive;
	private volatile int id = 0;
	
	private static final Logger                                    debugLog       = Logger.getLogger("jutil");
	private static final int                                       CHECK_INTERVAL = 5 * 1000;
	private static final ConcurrentHashMap<String, ObjectBalancer> m              = new ConcurrentHashMap<String, ObjectBalancer>();
	private static final Thread                                    t              = new Thread(){
		public void run()
		{
			while(true){
				try{
					checkAll();
					Thread.sleep(CHECK_INTERVAL);
				}catch(Throwable e){
				}
			}
		}
	};
	static
	{
		t.start();
	}
	
	public ObjectBalancer(String monName, E[] obj, Rule[] rule)
	{
		if(obj.length != rule.length)
			throw new RuntimeException("length not match. obj.length: " + obj.length + ", rule.length: " + rule.length);
		this.objs = obj;
		this.mons = new Monitor[rule.length];
		this.isAlive = new boolean[obj.length];
		Arrays.fill(isAlive, true);
		for(int i = 0; i < mons.length; ++i){
			final int idx = i;
			mons[i] = MonitorFactory.createMonitor(monName + "_" + i, rule[i], new Switcher(){
				public boolean doSwitch()
				{
					isAlive[idx] = false;
					return true;
				}
			});
		}
		m.put(monName, this);
	}
	
	private static void checkAll()
	{
		Set<Map.Entry<String, ObjectBalancer>> entrySet = m.entrySet();
		Iterator<Map.Entry<String, ObjectBalancer>> it = entrySet.iterator();

		while (it.hasNext())
		{
			try{
				Map.Entry<String, ObjectBalancer> entry = it.next();
				ObjectBalancer v = entry.getValue();
				v.checkDead();
			}catch(Throwable e){
				debugLog.error("", e);
			}
		}
	}
	
	public void checkDead()
	{
		for(int i = 0; i < isAlive.length; ++i)
		{
			if(!isAlive[i])
			{
				if(objs[i].isAlive())
					isAlive[i] = true;
			}
		}
	}
	
	public Pair<E, Monitor> get()
	{
		++id;
		int n = id % objs.length;
		if(isAlive[n])
			return Pair.makePair(objs[n], mons[n]);
		for(int i = 1; i < isAlive.length; ++i)
		{
			n = (n + 1) % objs.length;
			if(isAlive[n])
				return Pair.makePair(objs[n], mons[n]);
		}
		return Pair.makePair(objs[n], mons[n]);
	}
}
