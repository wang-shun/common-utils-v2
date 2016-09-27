package com.youzan.sz.jutil.jcache.admin;

import com.youzan.sz.jutil.admin.AdminCommand;
import com.youzan.sz.jutil.jcache.Cache;
import com.youzan.sz.jutil.jcache.CacheFactory;

import java.io.PrintWriter;
import java.util.Iterator;

//import com.qq.jutil.admin.AdminCommand;
//import com.qq.jutil.jcache.Cache;
//import com.qq.jutil.jcache.CacheFactory;

/**
 * cache管理命令封装
 * @author meteorchen
 *
 */
@SuppressWarnings("unchecked")
public class CacheInfo implements AdminCommand
{
	public void execute(String[] argv, PrintWriter out)
	{
		try
		{
			if(argv.length == 0)
			{//list cache
				list(out);
			}
			else if(argv.length == 2 && "-c".equalsIgnoreCase(argv[0]))
			{
				listKeys(argv[1], out);			
			}			
			else if(argv.length == 3 && "-c".equalsIgnoreCase(argv[0]))
			{
				if(argv[2].matches("[\\d]+"))
				{
					showValue(argv[1], Integer.parseInt(argv[2]), out);
				}
				else
				{
					showValue(argv[1], argv[2], out);
				}
			}
			else if(argv.length == 4 && "-c".equalsIgnoreCase(argv[0]))
			{
				if("-i".equalsIgnoreCase(argv[1]))
				{
					try
					{
						int key = Integer.parseInt(argv[3]);
						showValue(argv[2], key, out);
					}
					catch(Exception e)
					{
						out.println("invalid argment.");
					}
				}
				else if("-s".equalsIgnoreCase(argv[1]))
				{
					showValue(argv[2], argv[3], out);
				}
				else
				{
					out.println("invalid argment.");	
				}
			}
			else if(argv.length == 2 && "-d".equalsIgnoreCase(argv[0]))
			{
				clearCache(argv[1], out);			
			}		
			else if(argv.length == 3 && "-d".equalsIgnoreCase(argv[0]))
			{
				if(argv[2].matches("[\\d]+"))
				{
					showValue(argv[1], Integer.parseInt(argv[2]), out);
				}
				else
				{
					removeCache(argv[1], argv[2], out);
				}
				
			}
			else if(argv.length == 4 && "-d".equalsIgnoreCase(argv[0]))
			{
				if("-i".equalsIgnoreCase(argv[1]))
				{
					try
					{
						int key = Integer.parseInt(argv[3]);
						removeCache(argv[2], key, out);
					}
					catch(Exception e)
					{
						out.println("invalid argment.");
					}
				}
				else if("-s".equalsIgnoreCase(argv[1]))
				{
					removeCache(argv[2], argv[3], out);
				}
				else
				{
					out.println("invalid argment.");	
				}
			}
			else
			{
				printHelp(out);
			}
		}
		catch(Exception e)
		{
			out.println("Exception:"+ e.getMessage());
		}
	}

	private void printHelp(PrintWriter out)
	{
		out.println("\nUsage:");
		out.println("\tlist all cache");
		out.println("-c [cacheName]\tshow cache keys");
		out.println("-c [cacheName] [key]\tshow cache info");
		out.println("-c -i [cacheName] [key]\tshow cache info, key as integer.");
		out.println("-c -s [cacheName] [key]\tshow cache info, key as string");
		out.println("-d [cacheName]\tdelete cache name cacheName");
		out.println("-d [cacheName] [key]\tdelete cache item by key");
		out.println("-d -i [cacheName] [key]\tdelete cache item by key, key as integer");
		out.println("-d -s [cacheName] [key]\tdelete cache item by key, key as string");
		out.println("?\thelp");
	}
	
	private void removeCache(String cacheName, String key, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(cacheName);
		if(c != null)
		{
			c.remove(key);
		}
		else
		{
			out.println("Cache " + cacheName + " not exist.");
		}
	}
	
	private void removeCache(String cacheName, int key, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(cacheName);
		if(c != null)
		{
			c.remove(key);
		}
		else
		{
			out.println("Cache " + cacheName + " not exist.");
		}
	}

	private void clearCache(String cacheName, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(cacheName);
		if(c != null)
		{
			c.clear();
			out.println("delete cache "+ cacheName +" success");	
		}
		else
		{
			out.println("Cache "+ cacheName +" not exist.");
		}			
	}
	
	private void showValue(String cacheName, String key, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(cacheName);
		if(c != null)
		{
			out.println(c.get(key));
		}
		else
		{
			out.println("Cache " + cacheName + " not exist.");
		}
	}
	
	private void showValue(String cacheName, int key, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(cacheName);
		if(c != null)
		{
			out.println(c.get(key));
		}
		else
		{
			out.println("Cache " + cacheName + " not exist.");
		}			
	}
	
	private void listKeys(String name, PrintWriter out)
	{
		Cache c = CacheFactory.getCache(name);
		if(c != null)
		{
			out.println("keys:");
			Iterator it = c.keySet().iterator();
			while(it.hasNext())
			{
				out.println(it.next());
			}
		}
		else
		{
			out.println("Cache "+ name +" not exist.");
		}
	}

	private void list(PrintWriter out)
	{
		String outStr = "CacheName\t\t\t\tQueryCount\tHitCount\tHitRate\tSize\r\n";
		String[] names = CacheFactory.getAllCacheName();
		for (int i = 0; i < names.length; ++i)
		{
			Cache c = CacheFactory.getCache(names[i]);
			if(c == null)
			{
				outStr += names[i] +"\t\t\t\tNULL\n";
			}
			else
			{
				int qc = c.getQueryCount();
				int hc = c.getHitCount();
				String rate = qc > 0 ? (((double) hc * 100) / qc) +"" : "N/A";			
				outStr += names[i] + "\t\t\t\t" + qc + "\t" + hc + "\t"
						+ rate + "%\t" + c.size() + "\r\n";
			}
		}
		out.println(outStr);
	}
	
	public String toString()
	{
		return "show cache info.";
	}
}
