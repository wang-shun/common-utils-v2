package com.youzan.sz.jutil.oss;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 一组计数器，存放一个service的调用次数,成功次数，超时次数等
 * @author junehuang
 *
 */
public class Counter {
	private AtomicLongArray counters = new AtomicLongArray(4);//0-tatal,1-succ,2-timeout,3-totalCostTime
	private long lastResetTime = System.currentTimeMillis();//上次重置时间，用于计算时间间隔
	private int slowTime = 1000;//调用的超时时间，缺省1秒算超时
	
	public int getSlowTime() {
		return slowTime;
	}
	public void setSlowTime(int slowTime) {
		this.slowTime = slowTime;
	}
	public long getTotal(){
		return counters.get(0);
	}
	public long getSuccess(){
		return counters.get(1);
	}
	public long getTimeout(){
	    return counters.get(2);
	}
	public long getTotalCostTime(){
	    return counters.get(3);
	}
	public void addSuccess(long costTime){
		AtomicLongArray counters = getCounterArray();
		counters.incrementAndGet(0);
		counters.incrementAndGet(1);
	    if(slowTime > 0)
	    {
	        if(costTime > slowTime)
	        {
	    	    counters.incrementAndGet(2);
	        }
	    }
	    counters.addAndGet(3, costTime);
	}
	public void addFailed(long costTime){
		AtomicLongArray counters = getCounterArray();
		counters.incrementAndGet(0);
        if(slowTime > 0)
        {
            if(costTime > slowTime)
            {
        	    counters.incrementAndGet(2);
            }
        }
	    counters.addAndGet(3, costTime);
	}
	public void reset(){
		counters = new AtomicLongArray(4);
		lastResetTime = System.currentTimeMillis();
	}
	
	public long getLastResetTime(){
		return lastResetTime;
	}
	
	@Override
	public String toString() {
		return counters.get(0) + "," + counters.get(1) + "," + counters.get(2) + "," + counters.get(3);
	}
	
	private AtomicLongArray getCounterArray() {
		return this.counters;
	}
	
	public static void main(String[] args) {
		Counter counters = new Counter();
		counters.setSlowTime(5000);
		counters.addFailed(2000);
		counters.addFailed(2000);
		counters.addFailed(2000);
		counters.addSuccess(2000);
		System.out.println(counters);
		
		counters.reset();
		System.out.println(counters);
		
		counters.addFailed(2000);
		counters.addFailed(2000);
		counters.addSuccess(2000);
		counters.addFailed(2000);
		counters.addFailed(2000);
		counters.addSuccess(2000);
		System.out.println(counters);
	}
}
