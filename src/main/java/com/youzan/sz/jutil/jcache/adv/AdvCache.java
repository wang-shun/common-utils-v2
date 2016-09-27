package com.youzan.sz.jutil.jcache.adv;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.CacheFactory;
import com.youzan.sz.jutil.jcache.monitor.Visitable;
import com.youzan.sz.jutil.jcache.monitor.Visitor;
import com.youzan.sz.jutil.string.StringUtil;
import com.youzan.sz.jutil.util.Tools;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.CacheFactory;
//import com.qq.jutil.jcache.monitor.Visitable;
//import com.qq.jutil.jcache.monitor.Visitor;
//import com.qq.jutil.string.StringUtil;
//import com.qq.jutil.util.Tools;

/**
 * 改进的cache实现
 *   比CommonCache更合理、更高效的超时、淘汰策略
 *   支持持久化选项，每隔一段时间将cache内容保存到磁盘，程序重启时可以恢复
 *   支持自定义超时、淘汰处理
 * @author meteorchen
 *
 * @param <K>
 * @param <V>
 */
@SuppressWarnings("unchecked")
public class AdvCache<K, V> implements Cache<K, V>, Visitable
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private static final NullHandler nullHandler = new NullHandler();			// 默认的ItemHandler，直接删除超时、淘汰的元素
	private static final DefaultRecoverHandler defaultRecoverHandler = new DefaultRecoverHandler();	// 默认的恢复处理器

	private ConcurrentHashMap<K, Node<K, V> > map = new ConcurrentHashMap<K, Node<K, V> >();	// 保存cache的key-value对应关系的map
	NodeList<K, V> nl = new NodeList<K, V>();									// 最后访问时间排序的链表
	
	Lock l = new ReentrantLock();												// 锁
	
	protected AtomicInteger queryCount = new AtomicInteger();					// 查询次数
	protected AtomicInteger hitCount = new AtomicInteger();						// 命中次数
	
	private long timeout = 60 * 60 * 1000 * 1000;								// 超时时间，如果是负数，则表示不超时
	private int maxSize = 10000;												// 元素个数的最大数
	private double cleanupRate = 0.3;											// 清除率，当cache达到最大元素个数时，清除的百分比

	private ItemHandler<K, V> handler = nullHandler;							// ItemHandler，当检测到超时会淘汰元素时的处理策略
	private RecoverHandler<K, V> recoverHandler = defaultRecoverHandler;		// 恢复处理器，当从磁盘恢复的时候，用于回调
	
	private String saveFilePath;												// 持久化保存路径，如果为null，表示不需要持久化
	private long saveInterval = 3600 * 1000;									// 持久化的时间间隔
	private boolean autoRecover = false;										// 是否自动从持久化数据中恢复
	private volatile long reflushTime = 0;
	//增加一个变量定义是否清理所有的超时的数据，因为现在的逻辑只按清理比例清理，过期的数据可能还保留在cache中，某些情况下是无法接受的
	private boolean cleanAllTimeoutNode=false;//原来就是false，不改变原有逻辑

	public AdvCache(Properties prop)
	{
		timeout = StringUtil.convertLong(prop.getProperty("timeout"), 60 * 60 * 1000);
		timeout *= 1000; // 转换成毫秒
		maxSize = StringUtil.convertInt(prop.getProperty("max_size"), 10000);
		cleanAllTimeoutNode=StringUtil.convertBoolean(prop.getProperty("clean_all_timeout_node"),false);

		int cleanup_rate = StringUtil.convertInt(prop.getProperty("cleanup_rate"), 30);
		cleanupRate = ((double) cleanup_rate) / 100;
		
		String handlerClass = prop.getProperty("item_handler_class");
		if(handlerClass != null){
			try
			{
				ItemHandler<K, V> h = (ItemHandler<K, V>) createHandler(handlerClass);
				if(h != null)
					handler = h;
			}
			catch (Throwable e)
			{
				logger.error("create AdvCache error: ", e);
			}
		}
		
		String recoverHandlerClass = prop.getProperty("recover_handler_class");
		if(recoverHandlerClass != null){
			try
			{
				RecoverHandler<K, V> h = (RecoverHandler<K, V>) createHandler(recoverHandlerClass);
				if(h != null)
					recoverHandler = h;
			}
			catch (Throwable e)
			{
				logger.error("create AdvCache error: ", e);
			}
		}
		
		saveFilePath = Tools.getFilePath( prop.getProperty("save_file_path") );
		if( saveFilePath.length() == 0 )
			saveFilePath = null;
		autoRecover = getPropertyBoolean(prop, "auto_recover", false);
		// 如果设置了持久化选项
		if(saveFilePath != null){
			saveInterval = StringUtil.convertLong(prop.getProperty("save_interval"), 3600);
			saveInterval *= 1000;

			// 启动保存线程
			Thread saveThread = new SaveThread();
			saveThread.start();
		}
	}
	
	private class SaveThread extends Thread
	{
		public void run()
		{
			// 恢复cache
			if(autoRecover && saveFilePath != null)
				recover(saveFilePath);
			
			while(true)
			{
				try
				{
					Thread.sleep(saveInterval);
					save(saveFilePath);
				}
				catch (Throwable e)
				{
					logger.error("[SaveThread]save exception:", e);
				}
			}
		}
	};
	
	public AdvCache(long timeout, int maxSize, double cleanupRate)
	{
		this.timeout = timeout;
		this.maxSize = maxSize;
		this.cleanupRate = cleanupRate;
	}
	
	public AdvCache(long timeout, int maxSize, double cleanupRate, ItemHandler<K, V> handler)
	{
		this.timeout = timeout;
		this.maxSize = maxSize;
		this.cleanupRate = cleanupRate;
		this.handler = handler != null ? handler : nullHandler;
	}
	
	public AdvCache(long timeout, int maxSize, double cleanupRate, ItemHandler<K, V> handler, final String saveFilePath, final long saveInterval, final boolean autoRecover)
	{
		this.timeout = timeout;
		this.maxSize = maxSize;
		this.cleanupRate = cleanupRate;
		this.handler = handler != null ? handler : nullHandler;
		this.saveFilePath = saveFilePath;
		this.saveInterval = saveInterval;
		if(saveFilePath != null){
			// 启动保存线程
			Thread saveThread = new SaveThread();
			saveThread.start();
		}
	}
	
	private static boolean getPropertyBoolean(Properties prop, String name, boolean def)
	{
		String s = prop.getProperty(name);
		if("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
			return true;
		if("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s))
			return false;
		return def;
	}
	
	@SuppressWarnings("unchecked")
	private Object createHandler(String className) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Class c = Class.forName(className);
		if(c == null)
			return null;
		Constructor con = c.getConstructor();
		if(con == null)
			return null;
		return con.newInstance();
	}
	
	protected ItemHandler<K, V> getItemHandler()
	{
		return handler;
	}
	
	protected void setItemHandler(ItemHandler<K, V> h)
	{
		handler = h;
	}
	
	public V get(K key)
	{
		queryCount.incrementAndGet();
		l.lock();
		try{
			Node<K, V> n = map.get(key);
			if(n != null){
				hitCount.incrementAndGet();
				nl.moveToTop(n);
				return n.value;
			}
		}finally{
			l.unlock();
		}
		return null;
	}

	public boolean containsCache(K key)
	{
		return map.containsKey(key);
	}

	public V put(K key, V value)
	{
		Node<K, V> n = new Node<K, V>(key, value);
		n = put(n);
		return n != null ? n.value : null;
	}
	
	private Node<K, V> put(Node<K, V> n)
	{
		l.lock();
		try{
			Node<K, V> old = map.put(n.key, n);
			nl.add(n);
			if(old != null)
				nl.remove(old);
			return old;
		}finally{
			l.unlock();
		}
	}
	
	private boolean putIfNotExist(Node<K, V> n)
	{
		l.lock();
		try{
			Node<K, V> old = map.put(n.key, n);
			if(old != null){
				map.put(old.key, old);
				return false;
			}else{
				nl.add(n);
				return true;
			}
		}finally{
			l.unlock();
		}
	}

	public void clear()
	{
		this.queryCount.set(0);
		this.hitCount.set(0);
		
		l.lock();
		try{
			map.clear();
			nl.clear();
		}finally{
			l.unlock();
		}
	}

	public V remove(K key)
	{
		l.lock();
		try{
			Node<K, V> n = map.remove(key);
			if(n != null){
				nl.remove(n);
				return n.value;
			}
			return null;
		}finally{
			l.unlock();
		}
	}
	
	V remove(Node<K, V> n)
	{
		l.lock();
		try{
			Node<K, V> n1 = map.get(n.key);
			if(n1 == n){
				map.remove(n.key);
				nl.remove(n);
				return n.value;
			}
			return null;
		}finally{
			l.unlock();
		}
	}

	public int size()
	{
		return map.size();
	}

	/**
	 * 清除元素
	 * @param h				清除前需要做的操作
	 * @return				清除的元素数目
	 */
	public int cleanUp(ItemHandler<K, V> h)
	{
		// 删除超时的元素（按照创建时间）
		int c1 = 0;
		try{
			int newSize = (int)(((double) size()) * (1.0 - cleanupRate));
			if(timeout > 0){
				// 使用map的iterator而不是list的iterator来遍历，是为了避免元素被访问后移动到链表头时重复遍历的问题
				Set<Entry<K, Node<K, V>>> entrySet = map.entrySet();
				Iterator<Entry<K, Node<K, V>>> it = entrySet.iterator();
				while(it.hasNext() && (cleanAllTimeoutNode || (size() >= newSize) )){//如果是要清理所有超时的数据，则要遍历完所有的cache节点
					Entry<K, Node<K, V>> n = it.next();
					Node<K, V> node = n.getValue();
					long now = System.currentTimeMillis();
					if(now - node.createTime >= timeout){
						if(h.handle(node, true)){
							remove(node);
						}
						++c1;
					}
				}
			}
		}catch(Exception e){
			logger.error("AdvCache.cleanUp, clean timeout node error.", e);
		}
		
		// 按照最后访问时间淘汰
		int c2 = 0;
		try{
			int n = size();
			if(n > maxSize){
				final int newSize = (int)(((double) maxSize) * (1.0 - cleanupRate));
				for(; n - c2 > newSize; ++c2){
					Node<K, V> node = nl.tail;
					if(node == null)
						break;
					if(h.handle(node, false)){
						remove(node);
					}
				}
			}
		}catch(Exception e){
			logger.error("AdvCache.cleanUp, clean node by last access time error.", e);
		}
		return c1 + c2;
	}
	
	public int cleanUp()
	{
		logger.info("AdvCache.cleanUp");
		this.hitCount.set(0);
		this.queryCount.set(0);
		
		long now = System.currentTimeMillis();
		int c =  cleanUp(handler);
		reflushTime = System.currentTimeMillis() - now;
		return c;
	}

	public Set<K> keySet()
	{
		return map.keySet();
	}

	public int getQueryCount()
	{
		return queryCount.get();
	}

	public int getHitCount()
	{
		return hitCount.get();
	}

	public int getMemoryUsage()
	{
		return -1;
	}

	public Iterator<Entry<K, V>> iterator()
	{
		return new CacheIterator<K, V>(this);
	}
	
	public boolean recover(String file)
	{
		FileInputStream in = null;
		ObjectInputStream ois = null;
		try
		{
			File f = new File(file);
			if(!f.exists())
				return false;
			logger.info("recover cache from file: " + file);
			in = new FileInputStream(f);
			ois = new ObjectInputStream(in);
			long now = System.currentTimeMillis();
			NodeForPersistent<K, V> pr = null;
			while(true){
				try{
					pr = (NodeForPersistent<K, V>) ois.readObject();
				}catch(EOFException e){
					break;
				}
				if(pr == null)
					break;
				Node<K, V> n = new Node<K, V>(pr.key, pr.value, now - pr.timeDiff);
				if(recoverHandler.handle(n))
					putIfNotExist(n);
			}
		}
		catch (Throwable e)
		{
			logger.error("recover file error: " + file, e);
			return false;
		}
		finally
		{
			if(in != null){
				try
				{
					in.close();
				}
				catch (IOException e)
				{
					logger.error("close error: " + file, e);
				}
			}
			if(ois != null){
				try
				{
					ois.close();
				}
				catch (IOException e)
				{
					logger.error("close error: " + file, e);
				}
			}
		}
		return true;
	}
	
	public boolean save(String file)
	{
		logger.info("begin save cache to file: " + file);
		FileOutputStream outout = null;
		ObjectOutputStream oos = null;
		boolean success = true;
		int step = 0;//调试用
		try
		{
			String tmpFile = file + ".tmp";
			outout = new FileOutputStream(tmpFile);
			oos = new ObjectOutputStream(outout);
			Enumeration<Node<K, V>> en = map.elements();
			logger.info("cache size: " + map.size() +"\tfile:"+ tmpFile);
			long prevTime = System.currentTimeMillis();
			int count = 0;
			long now = prevTime;
			while(en.hasMoreElements())
			{
				step = 0;
				if(++count % 1000 == 0)
				{
					try
					{
						long st = (now - prevTime) / 2;
						st = st > 3000 ? 3000 : st;
						oos.flush();
						Thread.sleep(st);
						prevTime = now;
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				step = 1;
				Node<K, V> n = en.nextElement();
				step = 2;
				now = System.currentTimeMillis();
				try
				{
					step = 3;
					oos.writeObject(NodeForPersistent.make(n, now));
					step = 4;
				}
				catch(Throwable e)
				{
					logger.error("writeObject to file error: " + n.getValue().getClass().getName() + "," + file, e);
				}
			}
			step = 5;
			oos.flush();
			oos.close();
			step = 6;
			oos = null;
			String bakFile = file + ".bak";
			new File(file).renameTo(new File(bakFile));
			step = 7;
			new File(tmpFile).renameTo(new File(file));
			step = 8;
			new File(bakFile).delete();
			step = 9;
		}
		catch (Throwable e)
		{
			logger.error("save to file error: ", e);
			success = false;
			return false;
		}
		finally
		{
			if(outout != null){
				try
				{
					outout.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
			if(oos != null){
				try
				{
					oos.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
			logger.info("finish save cache to file: " + file +"\tresult:"+ success +"\tstep:"+ step);
		}
		
		return true;
	}
	
	/**
	 * 调试用,非常危险！！
	 * @return ConcurrentHashMap<K,Node<K,V>>
	 */
	public ConcurrentHashMap<K, Node<K, V> > getMap()
	{
		return this.map;
	}
	public static void main(String[] argv) throws InterruptedException
	{
		Cache<String, String> c = CacheFactory.getCache("example_adv_cache");
		for(int i = 0; i < 50; ++i){
			c.put("" + i, "");
		}
		Thread.sleep(11000);
		System.out.println("size: " + c.size());
		/*
		for(int i = 0; i < 1000; ++i){
			for(int j = 0; j < 10; ++j)
				c.put(RandomUtil.getString(2), "");
			Thread.sleep(10000);
			System.out.println("size: " + c.size());
		}
		*/
	}
	
	public long getReflushTime(){
		return this.reflushTime;
	}

	@Override
	public void accept(Visitor v, String name) {
		// TODO Auto-generated method stub
		v.visitAdvCache(this, name);
	}
}
