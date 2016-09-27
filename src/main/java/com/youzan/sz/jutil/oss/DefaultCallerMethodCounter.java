package com.youzan.sz.jutil.oss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.youzan.sz.jutil.util.Pair;

public class DefaultCallerMethodCounter implements CallerMethodCounter{
	
	private ConcurrentHashMap<String, Counter> map = new ConcurrentHashMap<String, Counter>();
	
	//实现CallerCounter接口
	public List< Pair<String,Counter> > getOssAllMethodCounter(){
		List< Pair<String,Counter> > list = new ArrayList< Pair<String,Counter> >();
		for( Entry<String,Counter> entry : map.entrySet() ){
			list.add( Pair.makePair(entry.getKey(), entry.getValue()) );
		}
		return list;
	}
	public Counter getOssOneMethodCounter(String method){
		Counter count = map.get(method);
		if( count==null ){
			count = new Counter();
			Counter old = map.putIfAbsent(method, count);
			if( old!=null )
				count = old;
		}
		return count;
	}
	
	public void addOssSuccess(String method,long costTime){
		getOssOneMethodCounter(method).addSuccess(costTime);
	}
	
	public void addOssFailed(String method,long costTime){
		getOssOneMethodCounter(method).addFailed(costTime);
	}
	
	
	public static void main(String[] args) {
		DefaultCallerMethodCounter counter = new DefaultCallerMethodCounter();
		counter.getOssOneMethodCounter("abc").setSlowTime(500);
		counter.addOssFailed("abc", 5000);
		counter.addOssSuccess("abc", 5000);
		counter.addOssSuccess("def", 1000);
		System.out.println(counter.getOssAllMethodCounter());
	}
}
