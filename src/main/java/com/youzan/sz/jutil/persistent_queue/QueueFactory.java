package com.youzan.sz.jutil.persistent_queue;

import com.youzan.sz.jutil.common.CongfigResource;
import com.youzan.sz.jutil.config.XMLConfigElement;
import com.youzan.sz.jutil.config.XMLConfigFile;
import com.youzan.sz.jutil.j4log.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//import com.qq.jutil.common.CongfigResource;
//import com.qq.jutil.config.XMLConfigElement;
//import com.qq.jutil.config.XMLConfigFile;
//import com.qq.jutil.j4log.Logger;

public final class QueueFactory
{
	public static final  Logger                       logger = Logger.getLogger("persistent_queue");
	private final static Map<String, PersistentQueue> map    = new HashMap<String, PersistentQueue>();
	
	static
	{
		init();
	}
	
	public static PersistentQueue getQuque(String name)
	{
		return map.get(name);
	}
	
	private static void init()
	{
    	XMLConfigFile xf = new XMLConfigFile();
		try
		{
			xf.parse(CongfigResource.loadConfigFile("persistent_quque.xml", QueueFactory.class));
		}
		catch (Exception e)
		{
			logger.error("init error",e);
		}
		XMLConfigElement root = xf.getRootElement();
		ArrayList<XMLConfigElement> list = root.getChildListByName("quque");	
		for (XMLConfigElement element : list)
		{
			String name = element.getStringAttribute("name");
			String dir = element.getStringAttribute("dir");
			int maxQueueSize = element.getIntAttribute("maxQueueSize", 10000);
			int initBlockCount = element.getIntAttribute("initBlockCount", 256);
			int blockSize = element.getIntAttribute("blockSize", 1024);
			int maxBlockCount = element.getIntAttribute("maxBlockCount", 100000);
			try
			{
				System.out.println("Mapping:"+ dir +"/"+ name +".buf");
				PersistentQueue v = new PersistentQueue(dir +"/"+ name +".buf",initBlockCount, blockSize, maxBlockCount, maxQueueSize);
				map.put(name, v);
			}
			catch (IOException e)
			{
				logger.error("Create quque:"+ name +" exception,dir:"+ dir,e);
			}
		}
	}
	public static void main(String[] args) throws Exception
	{
		//initBlockCount, int blockSize, int maxBlockCount, int maxQueueSize
		//PersistentQueue queue = new PersistentQueue("queue2", 256, 43, 100000, 100000);
		PersistentQueue queue = new PersistentQueue("queue1", 256, 1024, 100000, 100000);
		//ueue.put("aa", new byte[]{1,2});
		//queue.append("aa", new byte[]{0,0});
		for (int i = 0; i < 100000; i++)
		{
			try
			{
				queue.put("137566"+i, new byte[]
				{ 1, 2,2,2,2,2,2,2 });
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}			
		}
		System.out.println(Arrays.toString(queue.get("aa1")));
	}
}
