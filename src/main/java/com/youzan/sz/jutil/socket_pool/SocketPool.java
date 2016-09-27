package com.youzan.sz.jutil.socket_pool;

import com.youzan.sz.jutil.j4log.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.Logger;

/**
 * TCP连接池
 * @author meteorchen
 *
 */
public final class SocketPool
{
	private static Random rnd = new Random();
	private static double randRange = 0.2;
	
	private ConcurrentLinkedQueue<SocketWrapper> queue = new ConcurrentLinkedQueue<SocketWrapper>();	// socket队列	
	private int maxAliveTime = 60 * 60 * 1000;		// 连接的最大生存时间
	private int maxUseCount = 5000;					// 连接的最大使用次数
	private AtomicInteger size = new AtomicInteger(0);	// 连接池的大小
	private SocketAddress addr;							// 目标地址
	private volatile boolean closeFlag = false;		// 连接池是否已经关闭
	private int socketTimeout = 5000;				// 超时时间
	private int connetTimeout = 5000;  // 连接超时时间
	private volatile int poolSize = -1;						// 连接池最大size,<=0表示无限制
	
	private static final Logger debugLog = Logger.getLogger("jutil");

    public SocketPool(InetAddress address, int port, int maxAliveTime, int maxUseCount, int connectTimeout, int socketTimeout, int poolSize)
    {
        this.addr = new InetSocketAddress(address, port);
        this.maxAliveTime = maxAliveTime;
        this.maxUseCount = maxUseCount;
        this.connetTimeout = connectTimeout;
        this.socketTimeout = socketTimeout;
        this.poolSize =  poolSize; 
    }
    
	public SocketPool(InetAddress address, int port, int maxAliveTime, int maxUseCount, int socketTimeout, int poolSize)
	{
		this(address, port, maxAliveTime, maxUseCount, socketTimeout, socketTimeout, poolSize);
	}
	
	public SocketPool(InetAddress address, int port, int maxAliveTime, int maxUseCount, int socketTimeout)
	{
		this(address, port, maxAliveTime, maxUseCount, socketTimeout, socketTimeout, -1);
	}
	
	int getTimeRandomInit()
	{
		int rd = (int)(maxAliveTime * randRange * 2);
		return rnd.nextInt(rd) - rd / 2;
	}
	
	int getUseCountRandomInit()
	{
		int rd = (int)(maxUseCount * randRange * 2);
		return rnd.nextInt(rd) - rd / 2;
	}
	
	public void setMaxPoolSize(int n)
	{
		this.poolSize = n;
	}
	
	public SocketWrapper getConnection() 
	{
		while(true) {
		    long t1 = System.currentTimeMillis();
			try
			{
				SocketWrapper con = queue.poll();
				if (con == null)
				{
					if(this.poolSize > 0 && size.get() >= this.poolSize)
					{
						throw new RuntimeException("Too manny socket!maxSize:"+ this.poolSize +"\t now size:"+ this.size());
					}
					Socket s = new Socket();
					s.setSoTimeout(socketTimeout);
					s.connect(addr, connetTimeout);
					s.setTcpNoDelay(true);
					con = new SocketWrapper(s, this);
					size.incrementAndGet();
					return con;
				}
				else if (isValid(con))
				{
					return con;
				}
				else
				{
					closeReal(con);
				}
			}
			catch (Exception e)
			{
				debugLog.error("SocketPool.getConnection error(addr:"+addr+", connectTimeout:"+connetTimeout+", soTimeout:"+socketTimeout+", maxPoolSize:"+poolSize+", currentPoolSize:"+size.get()+", taketime:"+(System.currentTimeMillis()-t1)+"): ", e);
				throw new RuntimeException(e);
			}
		}
	}

	public void closeAll()
	{
		closeFlag = true;
		ConcurrentLinkedQueue<SocketWrapper> q = queue;
		Iterator<SocketWrapper> i = q.iterator();
		while(i.hasNext()){
			SocketWrapper s = i.next();
			closeReal(s);
		}
	}

	/**
	 * 慎用！！本方法只能清理掉连接池内空闲的连接，但是在清理期间已经借出去的连接会当作好连接还回来，只有你确定连接都已还回池子或者不在乎这些误差时，才用本方法
	 */
    public void reset()
    {
        closeFlag = true;
        ConcurrentLinkedQueue<SocketWrapper> q = queue;
        while(true)
        {
            SocketWrapper s = q.poll();
            if(s == null)
            {
                break;
            }
            closeReal(s);
        }
        closeFlag = false;
    }

	void closeReal(SocketWrapper con)
	{
		size.decrementAndGet();
		try
		{
			con.con.close();
		}
		catch (Exception e)
		{
			debugLog.error("closeReal error: ", e);
		}
	}
	
	public void setSocketTimeout(int timeout)
	{
		this.socketTimeout = timeout;
	}
	
	private boolean isValid(SocketWrapper con)
	{
		if(closeFlag)
			return false;
		long now = System.currentTimeMillis();
		if (now - con.createTime > maxAliveTime)
			return false;
		return con.useCount <= maxUseCount;
	}
	
	boolean closeConnection(SocketWrapper con)
	{
		boolean r = isValid(con);
		if(r){
			queue.add(con);
		}else{
			closeReal(con);
		}
		return !r;
	}

	public int getMaxAliveTime()
	{
		return maxAliveTime;
	}

	public int getMaxUseCount()
	{
		return maxUseCount;
	}

	public int size()
	{
		return size.get();
	}
	public SocketAddress getAddress()
	{
		return this.addr;
	}
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{")
			.append("maxAliveTime: ").append(maxAliveTime)
			.append(", maxUseCount: ").append(maxUseCount)
			.append(", size: ").append(size.get())
			.append(", freeCount: ").append(queue.size())
			.append(", addr: ").append(addr)
			.append("}");
		return sb.toString();
	}

	public void setMaxAliveTime(int maxAliveTime)
	{
		this.maxAliveTime = maxAliveTime;
	}

	public void setMaxUseCount(int maxUseCount)
	{
		this.maxUseCount = maxUseCount;
	}
	
	public static void main(String[] argv) throws UnknownHostException
	{
		SocketPool sp = new SocketPool(InetAddress.getByName("192.168.1.13"), 50001, 3600 * 1000, 5000, 5000);
		for(int i = 0; i < 1000; ++i){
			SocketWrapper s = sp.getConnection();
			s.close();
			System.out.println(i + "\tsize: " + sp.size() + "\tfree: " + sp.queue.size());
		}
		System.out.println("size: " + sp.size() + "\tfree: " + sp.queue.size());
		System.exit(0);
	}
}
