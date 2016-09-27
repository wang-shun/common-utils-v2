package com.youzan.sz.jutil.common;

import com.youzan.sz.jutil.j4log.Logger;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;

//import com.qq.jutil.j4log.Logger;

/**
 * 读取classpath下的文件
 * 一般用于读取classpath下的配置文件,注意读取的配置文件路径是相对传入参数clazz的load入路径.
 * 例：<br>
 * ClassTest c1;//假设c1是从/usr/local/classpath/   载入<br>
 * ClassTest c2;//假设c2是从/home/classpath/   载入<br>
 * ............<br>
 * loadConfigFile("a.xml",c1.class);//从/usr/local/classpath1/下寻找a.xml<br>
 * loadConfigFile("a.xml",c2.class);//从/home/classpath/下寻找a.xml<br>
 * @author stonexie
 *
 */
public class CongfigResource 
{
	private static final Logger debugLog = Logger.getLogger("jutil");
	
	/**
	 * 读取classpath下的文件
	 * @param resource	资源路径	
	 * @param clazz	
	 * @return InputStream 文件的inputstram,如果找不到则返回null
	 */
	@SuppressWarnings("unchecked")
	public static InputStream loadConfigFile(String resource,Class clazz)
	{
	  	ClassLoader classLoader =  null;
	    try 
	    {
	    	Method method = Thread.class.getMethod("getContextClassLoader");
	    	classLoader =  (ClassLoader) method.invoke(Thread.currentThread());
	    }
	    catch (Exception e) 
	    {
	    	debugLog.error("loadConfigFile error: ", e);
	    }
		if(classLoader == null)
		{
			classLoader = clazz.getClassLoader(); 	
		}
		try 
		{
			if (classLoader != null) 
			{
				URL url = classLoader.getResource(resource);
				if(url == null)
				{
					System.out.println("Can not find resource:"+ resource);
					return null;
				}
				if(url.toString().startsWith("jar:file:"))
				{
					System.out.println("Get resource \""+ resource + "\" from jar:\t" + url.toString());					
					return clazz.getResourceAsStream(resource.startsWith("/") ? resource : "/"+ resource);
				}
				else
				{
					System.out.println("Get resource \""+ resource + "\" from:\t" + url.toString());					
					return new FileInputStream(new File(url.toURI()));
				}
			}			
		}
		catch(Exception e) 
		{
			debugLog.error("loadConfigFile error: ", e);
		}
		return null;
	}
}
