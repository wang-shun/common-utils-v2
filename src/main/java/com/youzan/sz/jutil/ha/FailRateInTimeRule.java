package com.youzan.sz.jutil.ha;

public class FailRateInTimeRule implements Rule
{
    volatile long lastClearTime;
	long timeslice = 60000;
	double failRate = 0.8;
	
	volatile int failCount;
	volatile int totalCount;
	
	public FailRateInTimeRule(long timeslice, double failRate)
	{
		this.lastClearTime = System.currentTimeMillis();
		this.timeslice = timeslice;
		this.failRate = failRate;
	}
	
	public boolean test(boolean succ)
	{
		long now = System.currentTimeMillis();
		if(now - lastClearTime > timeslice){
			lastClearTime = now;
			failCount = 0;
			totalCount = 0;
			return false;
		}
		++totalCount;
		if(!succ){
			double r = ((double)(++failCount)) / totalCount;
			if(r >= failRate){
				lastClearTime = now;
				//failCount = 0;
				//totalCount = 0;
				return true;
			}
		}
		return false;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{failCount: ").append(failCount)
			.append(", totalCount: ").append(totalCount)
			.append(", lastClearTime: ").append(lastClearTime)
			.append(", timeslice: ").append(timeslice)
			.append(", failRate: ").append(failRate)
			.append("}");
		return sb.toString();
	}
}
