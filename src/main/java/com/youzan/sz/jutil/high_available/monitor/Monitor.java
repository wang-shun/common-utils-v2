package com.youzan.sz.jutil.high_available.monitor;

//import com.qq.jutil.j4log.Logger;

import com.youzan.sz.jutil.j4log.Logger;

public class Monitor
{
	private static final Logger log = Logger.getLogger("jutil");
	
	private String name;
	private Monitorable m;
	private long checkInterval;
	private volatile int state = Monitorable.STATE_ALIVE;
	private long lastCheckTime = System.currentTimeMillis();

	Monitor(String name, Monitorable m, long checkInterval)
	{
		this.name = name;
		this.m = m;
		this.checkInterval = checkInterval;
	}
	
	public int getState()
	{
		return state;
	}
	
	public void setState(int state)
	{
		this.state = state;
	}
	
	boolean timeToCheck()
	{
		long now = System.currentTimeMillis();
		boolean r = now - lastCheckTime > checkInterval;
		if(r)
			lastCheckTime = now;
		return r;
	}

	void checkAlive()
	{
		if(state == Monitorable.STATE_ALIVE)
		{
			if(m.checkAlives() != Monitorable.STATE_ALIVE)
			{
				log.warn("alive to dead, name: " + name + ", state: " + state);
				if(m.onChangeState(state, Monitorable.STATE_DEAD))
					state = Monitorable.STATE_DEAD;
				this.lastCheckTime = System.currentTimeMillis();
			}
		}
	}

	void checkDead()
	{
		if(state != Monitorable.STATE_ALIVE)
		{
			if(m.checkDeads() != Monitorable.STATE_DEAD)
			{
				log.warn("dead to alive, name: " + name + ", state: " + state);
				if(m.onChangeState(state, Monitorable.STATE_ALIVE))
					state = Monitorable.STATE_ALIVE;
				this.lastCheckTime = System.currentTimeMillis();
			}
		}
	}
}
