package com.youzan.sz.jutil.high_available.heartbeat.test;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

import com.youzan.sz.jutil.high_available.heartbeat.HeartBeat;
import com.youzan.sz.jutil.high_available.heartbeat.HostEvent;

public class Test1
{
	public static void main(String[] args) throws UnknownHostException, SocketException
	{
		HeartBeat hb = new HeartBeat("127.0.0.1", 22222, 22223, new HostEvent(){
			public void onStateChange(int newState, int oldState)
			{
				System.err.println("---remote host state change---, newState: " + newState + ", oldState: " + oldState + ", now: " + new Date());
			}

			public void onLocalStateNotify(int remote, int local)
			{
				System.err.println("---state change notify---, remote: " + remote + ", local: " + local + ", now: " + new Date());				
			}
		});
		hb.run();
	}
}
