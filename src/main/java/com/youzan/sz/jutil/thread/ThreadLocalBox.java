package com.youzan.sz.jutil.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 这个就相当于JDK的ThreadLocal类，唯一的区别在于我们不是弱引用，用完了以后需要显式去调用remove()方法，否则会内存泄漏
 * @author junehuang
 *
 */
public class ThreadLocalBox {
	//染色的维度
	public static String HTTP_MOBILE = "HTTP_MOBILE";
	public static String HTTP_USER_AGENT = "HTTP_USER_AGENT";
	public static String HTTP_Q_UA = "HTTP_Q_UA";
	public static String HTTP_REMOTE_IP = "HTTP_REMOTE_IP";
	public static String SESSION_UIN = "SESSION_UIN";
	public static String HTTP_REFER = "HTTP_REFER";
	
	private static ThreadLocalBox instance = new ThreadLocalBox();
	
	private ConcurrentHashMap<Thread, Map<String,Object>> map = new ConcurrentHashMap<Thread, Map<String,Object> >();

	private ThreadLocalBox(){
		//单例模式
	}
	
	public static ThreadLocalBox getInstance(){
		return instance;
	}
	
	/**
	 * 
	 * @param key 取值范围见ThreadLocalBox的静态变量
	 * @return
	 */
	public Object get( String key ){
		Map<String,Object> values = map.get(Thread.currentThread());
		if( values != null ){
			Object obj = values.get( key );
			if( obj != null ){
				return obj;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param key 取值范围见ThreadLocalBox的静态变量
	 * @param obj
	 */
	public void put(String key, Object obj){
		Map<String,Object> values = map.get(Thread.currentThread());
		if( values == null ){
			HashMap<String,Object> tmp = new HashMap<String,Object>();
			map.put(Thread.currentThread(), tmp);
			values = tmp;
		}
		values.put(key, obj);//没有线程安全问题，因为values只可能这个线程来操作，没有其他线程能够做。
	}
	
	/**
	 * 删除线程所有局部变量
	 */
	public void remove(){
		map.remove( Thread.currentThread() );
	}
	
	/**
	 * 返回所有调用了put()，但是未调用remove()的线程数量，用于调试
	 * @return
	 */
	public int getTotalSize(){
		return map.size();
	}
	
	public static void main(String[] args) {
		ThreadLocalBox box = ThreadLocalBox.getInstance();
		box.put(ThreadLocalBox.SESSION_UIN, 3752767);
		box.put(ThreadLocalBox.HTTP_MOBILE, "13925266821");
		int uin = (Integer)box.get(ThreadLocalBox.SESSION_UIN);
		String mobile = (String)box.get(ThreadLocalBox.HTTP_MOBILE);
		System.out.println( uin );
		System.out.println( mobile );
		System.out.println( box.get(ThreadLocalBox.HTTP_REFER) );
	}
}
