package com.youzan.sz.jutil.socket_pool;

import com.youzan.sz.jutil.common.CongfigResource;
import com.youzan.sz.jutil.config.XMLConfigElement;
import com.youzan.sz.jutil.config.XMLConfigFile;
import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.net.AddressUtil;

import java.util.ArrayList;
import java.util.HashMap;

//import com.qq.jutil.common.CongfigResource;
//import com.qq.jutil.config.XMLConfigElement;
//import com.qq.jutil.config.XMLConfigFile;
//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.net.AddressUtil;

/**
 * 连接池
 * 在配置文件socketpool.xml配置
 * @author stonexie
 *
 */
public class SocketPoolFactory
{
	private static final Logger logger = Logger.getLogger("jutil");
	
	private static final String SERVER_CONF = "socketpool.xml";
	private static HashMap<String, SocketPool> socketePool = new HashMap<String, SocketPool>();
	
	static
	{
		init();
	}
	/**
	 * 取得连接池
	 * @param poolName
	 * @return SocketPool
	 */
	public static SocketPool getSocketPool(String poolName)
	{
		SocketPool pool = socketePool.get(poolName); 
		if(pool == null)
		{
			logger.error("[SocketPoolFactory]pool name:"+ poolName +" not confige!Please confige it in socketpool.xml.");
		}
		return pool;
	}
	
	/**
	 * 初始化连接池
	 *  void
	 */
	public static void init()
	{
		logger.info("----init socket pool---");
		XMLConfigFile xf = new XMLConfigFile();
		try
		{
			xf.parse(CongfigResource.loadConfigFile(SERVER_CONF, SocketPoolFactory.class));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		XMLConfigElement root = xf.getRootElement();
		ArrayList<XMLConfigElement> lsPool = root.getChildListByName("socketpool");
		for(XMLConfigElement el : lsPool)
		{
			String name = el.getStringAttribute("name");
			String ip = el.getStringAttribute("ip");
			int port = el.getIntAttribute("port", 999);
			int maxAliveTime = el.getIntAttribute("maxAliveTime", 1800) * 1000;
			int maxUseCount = el.getIntAttribute("maxUseCount", 100000);
			int socketTimeout = el.getIntAttribute("socketTimeout", 100000);
            int connectTimeout = el.getIntAttribute("connectTimeout", socketTimeout);
			int poolSize = el.getIntAttribute("poolSize", -1);
			logger.info(name +"\t"+ ip +"\t"+ port +"\t"+ poolSize +"\t"+ maxAliveTime +"\t"
										+ maxUseCount +"\t"+ socketTimeout);
			socketePool.put(name,new SocketPool(AddressUtil.ip2Addr(ip), port, maxAliveTime,
							maxUseCount, connectTimeout, socketTimeout, poolSize));
			
		}
	}
}
