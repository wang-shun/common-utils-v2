package com.youzan.sz.jutil.high_available.heartbeat;

public interface HostEvent
{
	/**
	 * 当远程机器状态发生变化时，HeartBeat会调用该方法
	 * @param newState	新的状态
	 * @param oldState	旧状态
	 */
	void onStateChange(int newState, int oldState);
	
	/**
	 * 当本机状态发生变化时，HeartBeat会通知远程机器，远程机器会返回确认包，当本机收到变化确认时，会调用改方法
	 * @param remote	远程机器上本机的状态
	 * @param local		本机实际的状态
	 */
	void onLocalStateNotify(int remote, int local);
}
