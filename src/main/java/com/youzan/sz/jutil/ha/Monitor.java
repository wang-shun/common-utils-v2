package com.youzan.sz.jutil.ha;

//import com.qq.jutil.j4log.Logger;

import com.youzan.sz.jutil.j4log.Logger;

public class Monitor
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private String name;
	private Rule rule;
	private Switcher sw;
	
	Monitor(String name, Rule rule, Switcher sw)
	{
		this.name = name;
		this.rule = rule;
		this.sw = sw;
	}
	
	public void logResult(boolean succ)
	{
		if(rule.test(succ)){
			logger.warn("doSwitch: " + this);
			sw.doSwitch();
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{name: ")
			.append(name)
			.append(", rule: ")
			.append(rule)
			.append(", sw: ")
			.append(sw)
			.append("}");
		return sb.toString();
	}
}
