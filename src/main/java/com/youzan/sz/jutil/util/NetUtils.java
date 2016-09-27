package com.youzan.sz.jutil.util;

import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author oscarhuang
 *
 */
public final class NetUtils 
{
	/**
	 * 
	 */
	private final static Map<String, Integer> qosFlagMap = new HashMap<String, Integer>();
	
	/*
	 * 
	 */
	static
	{
		/**
		 * 1) 无线专线带宽实保障QOS:用:100-100-00(2进制) 10进制 144 16进制90
		 * 2) 无线非实时带宽(走传输平台): 10进制24 16进程:18
		 * 
		 */
		qosFlagMap.put("B1_REAL_TIME_QOS", 144); 	 //无线专线带宽实保障QOS
		qosFlagMap.put("B1_NON_REAL_TIME_QOS", 24);  //无线非实时带宽(走传输平台)
	}
	
	/**
	 * 设置Qos流量标识,使用前请找运维申请并通知平台组统一授权管理。
	 * 
	 * @param socket
	 * @param flag
	 */
	public static void setQosFlag(Socket socket , String qosConfigName) throws SocketException
	{
		Integer qosFlag = qosFlagMap.get(qosConfigName);
		if (qosFlag == null) throw new IllegalArgumentException("The config name {" + qosConfigName + "} is not authorized to access Qos configurations.");
		socket.setTrafficClass(qosFlag.intValue());
	}
	
	/**
	 * 获取Qos流量标示,使用前请找运维申请并通知平台组统一授权管理。
	 * 
	 * @param qosConfigName
	 */
	public static int getQosFlag(String qosConfigName)
	{
		Integer qosFlag = qosFlagMap.get(qosConfigName);
		if (qosFlag == null) throw new IllegalArgumentException("The config name {" + qosConfigName + "} is not authorized to access Qos configurations.");
		return qosFlag.intValue();
	}
}
