package com.youzan.sz.jutil.ha;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorFactory
{
	private static final Map<String, Monitor> m = new ConcurrentHashMap<String, Monitor>();
	
	public static Monitor createMonitor(String name, Rule rule, Switcher sw)
	{
		if(name == null || rule == null || sw == null)
			return null;
		Monitor mon = m.get(name);
		if(mon != null)
			return mon;
		mon = new Monitor(name, rule, sw);
		m.put(name, mon);
		return mon;
	}
	
	//add by Timtin, force to create new monitor, no matter whether this name monitor already exist
	public static Monitor updateMonitor(String name, Rule rule, Switcher sw)
	{
		if(name == null || rule == null || sw == null)
			return null;
		Monitor mon = new Monitor(name, rule, sw);
		m.put(name, mon);
		return mon;
	}
	
	public static Monitor getMonitor(String name)
	{
		return m.get(name);
	}
	
	public static void main(String[] argv)
	{
		ObjectSwitcher<String> osw = new ObjectSwitcher<String>("1", "2");
		System.out.println(osw.get());
		Monitor mon = MonitorFactory.createMonitor("test", new ContinuousFailRule(3), osw);
		mon.logResult(false);
		mon.logResult(false);
		mon.logResult(false);
		System.out.println(osw.get());
	}
}
