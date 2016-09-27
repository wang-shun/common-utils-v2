package com.youzan.sz.jutil.oss;

import com.youzan.sz.jutil.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.util.Pair;

public class DefaultCalleeIpMethodCounter implements CalleeIpMethodCounter{
	private ConcurrentHashMap<Pair<String, String>, Counter> map = new ConcurrentHashMap<Pair<String, String>, Counter>();

	public List<Pair<Pair<String, String>, Counter>> getOssAllIpMethodCounter() {
		List< Pair<Pair<String, String>,Counter> > list = new ArrayList< Pair<Pair<String, String>,Counter> >();
		for( Entry<Pair<String, String>,Counter> entry : map.entrySet() ){
			list.add( Pair.makePair(entry.getKey(), entry.getValue()) );
		}
		return list;
	}

	public Counter getOssOneIpMethodCounter(Pair<String, String> ipMethod) {
		Counter count = map.get(ipMethod);
		if( count==null ){
			count = new Counter();
			Counter old = map.putIfAbsent(ipMethod, count);
			if( old!=null )
				count = old;
		}
		return count;
	}
	
	public void addOssCalleeSuccess(Pair<String, String> ipMethod,long costTime){
		getOssOneIpMethodCounter(ipMethod).addSuccess(costTime);
	}
	
	public void addOssCalleeFailed(Pair<String, String> ipMethod,long costTime){
		getOssOneIpMethodCounter(ipMethod).addFailed(costTime);
	}
	
	public static void main(String[] args) {
		DefaultCalleeIpMethodCounter counter = new DefaultCalleeIpMethodCounter();
		counter.getOssOneIpMethodCounter( Pair.makePair("172.25.3.41", "abc")).setSlowTime(50000);
		counter.addOssCalleeFailed(Pair.makePair("172.25.3.41", "abc"), 5000);
		counter.addOssCalleeSuccess(Pair.makePair((String)null,(String)null), 500);
		counter.addOssCalleeSuccess(Pair.makePair((String)null,(String)null), 500);
		counter.addOssCalleeFailed(Pair.makePair((String)null,(String)null), 500);
		counter.addOssCalleeSuccess(Pair.makePair("172.25.3.42", "abc"), 1000);
		System.out.println(counter.getOssAllIpMethodCounter());
	}
}
