package com.youzan.sz.jutil.oss;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 一组计数器，存放一个service的调用次数和成功次数
 * @author huangjun
 *
 */
public class HessianRemoteObjectCounter {
	AtomicLongArray counters = new AtomicLongArray(5);
	long lastResetTime = System.currentTimeMillis();
	private int slowTimeout = 0;
	
	public int getSlowTimeout() {
		return slowTimeout;
	}
	public void setSlowTimeout(int slowTimeout) {
		this.slowTimeout = slowTimeout;
	}
	public long getTotal(){
		return counters.get(0);
	}
	public long getSuccess(){
		return counters.get(1);
	}
	public long getContinuousFail(){
		return counters.get(2);
	}
	public long getTimeout(){
	    return counters.get(3);
	}
	public long getContinuousTimeout(){
		return counters.get(4);
	}
	private void addSuccess(){
		counters.incrementAndGet(0);
		counters.incrementAndGet(1);
		counters.set(2, 0);
	}
	private void addFailed(){
		counters.incrementAndGet(0);
		counters.incrementAndGet(2);
	}
	public void addSuccess(long costTime){
	    addSuccess();
	    if(slowTimeout > 0)
	    {
	        if(costTime > slowTimeout)
	        {
	            addTimeout();
	        }
	        else
	        {
	            addInTime();
	        }
	    }
	}
	public void addFailed(long costTime){
	    addFailed();
        if(slowTimeout > 0)
        {
            if(costTime > slowTimeout)
            {
                addTimeout();
            }
            else
            {
                addInTime();
            }
        }
	}
	private void addTimeout(){
	    counters.incrementAndGet(3);
	    counters.incrementAndGet(4);
	}
	private void addInTime(){
        counters.set(4, 0);
	}
	
	public void reset(){
		counters = new AtomicLongArray(5);
		lastResetTime = System.currentTimeMillis();
	}
	
	public long getLastResetTime(){
		return lastResetTime;
	}
	
	@Override
	public String toString() {
		return counters.get(1) + "," + counters.get(0) + "," + counters.get(2) + "," + counters.get(3) + "," + counters.get(4);
	}
	
	public static void main(String[] args) {
		HessianRemoteObjectCounter counters = new HessianRemoteObjectCounter();
		counters.addFailed();
		counters.addFailed();
		counters.addFailed();
		counters.addSuccess();
		System.out.println(counters);
		
		counters.reset();
		System.out.println(counters);
		
		counters.addFailed();
		counters.addFailed();
		counters.addSuccess();
		counters.addFailed();
		counters.addFailed();
		counters.addSuccess();
		System.out.println(counters);
	}
}
