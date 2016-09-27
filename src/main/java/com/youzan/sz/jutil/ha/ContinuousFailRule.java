package com.youzan.sz.jutil.ha;

public class ContinuousFailRule implements Rule
{
	private int failLimit = 100;
	private volatile int failCount;
	
	public ContinuousFailRule(int failLimit)
	{
		this.failLimit = failLimit;
	}
	
	public boolean test(boolean succ)
	{
		if(!succ){
			if(++failCount >= failLimit){
				//failCount = 0;
				return true;
			}
		}else{
			failCount = 0;
		}
		return false;
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{failCount: ").append(failCount)
			.append(", failLimit: ").append(failLimit)
			.append("}");
		return sb.toString();
	}
}
