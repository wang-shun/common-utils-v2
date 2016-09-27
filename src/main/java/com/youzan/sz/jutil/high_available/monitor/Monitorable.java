package com.youzan.sz.jutil.high_available.monitor;

public interface Monitorable
{
	public static final int STATE_ALIVE = 0;
	public static final int STATE_DEAD = -1;
	
	/**
	 * 检查存活机器是否发生故障
	 * @return	返回当前状态
	 */
	int checkAlives();
	
	/**
	 * 检查故障机器是否恢复
	 * @return	返回当前状态
	 */
	int checkDeads();
	
	/**
	 * 当框架检测到该机器发生状态切换时，将调用该方法通知应用层
	 * @param oldState	切换前的状态
	 * @param newState	切换后的状态
	 * @return			是否需要修改本地状态
	 */
	boolean onChangeState(int oldState, int newState);
}
