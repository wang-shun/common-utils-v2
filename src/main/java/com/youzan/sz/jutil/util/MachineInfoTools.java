package com.youzan.sz.jutil.util;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.string.StringUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.string.StringUtil;

/**
 * 
 * @author kenwaychen
 *
 */
public class MachineInfoTools {

	/**
	 * 机器信息配置文件，在AMC对机器进行初始化的时候就写好的
	 */
	private static final String MACHINE_CONF_FILE = "/usr/local/support/conf/machine.info";

	/**
	 * 机器信息工具类日志
	 */
	private static final Logger machineInfoLog = Logger.getLogger("jutil_machineInfoLog");

	/**
	 * 存放机器信息的map
	 */
	private static ConcurrentHashMap<String, String> machineInfoMap = new ConcurrentHashMap<String, String>();

	/**
	 * 本机主内网IP的key
	 */
	private static String LOCALIP_KEY = "inner_ip";

	/**
	 * 本机所在IDC的key
	 */
	private static String IDC_KEY = "idc_city";

	/**
	 * 仅仅在类加载的时候初始化一次
	 */
	static {
		try {
			loadMachineInfo();
		} catch (Throwable e) {
			machineInfoLog.fatal("loadMachineInfo exception", e);
		}
	}

	/**
	 * 读取机器配置信息
	 */
	private static void loadMachineInfo() {
		InputStream in = null;
		Reader reader = null;
		try {
			Properties prop = new Properties();
			in = new FileInputStream(MACHINE_CONF_FILE);
			reader = new InputStreamReader(in, StringUtil.UTF_8);
			prop.load(reader);
			fillMachineInfo(prop);
		} catch (Throwable e) {
			machineInfoLog.fatal("Fatal! Can't load the " + MACHINE_CONF_FILE + " !", e);
			System.err.println("Fatal! Can't load the " + MACHINE_CONF_FILE + " !");
			e.printStackTrace();
		} finally {
			try {
				if (null != reader)
					reader.close();
				if (null != in)
					in.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * 填充机器信息到工具类
	 * 
	 * @param prop
	 */
	private static void fillMachineInfo(Properties prop) {
		// TODO 当前仅填充内网IP，如果需要其他信息再完善这个方法，原则是一个key读取失败不能让其余的都失败
		machineInfoMap.put(LOCALIP_KEY, trimInfo(LOCALIP_KEY, prop));
		machineInfoMap.put(IDC_KEY, trimInfo(IDC_KEY, prop));
	}

	/**
	 * @param key
	 * @param prop
	 * @return
	 */
	private static String trimInfo(String key, Properties prop) {
		return StringUtil.trim(prop.getProperty(key, null));
	}

	/**
	 * 获取机器配置信息，获取不到则抛异常
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private static String getMachineInfo(String key) throws Exception {
		String value = machineInfoMap.get(key);
		if (null == value)
			throw new Exception("Fatal! Can't load the " + key + " from " + MACHINE_CONF_FILE + " !");
		return value;
	}

	/**
	 * 获取本机主内网IP，如果获取不到则抛出异常，只适用于我们部门的服务器
	 * 
	 * @return
	 */
	public static String getLocalIP() throws Exception {
		return getMachineInfo(LOCALIP_KEY);
	}

	/**
	 * 获取本机所在IDC，如果获取不到则抛出异常，只适用于我们部门的服务器
	 * 
	 * @return
	 */
	public static String getIDC() throws Exception {
		return getMachineInfo(IDC_KEY);
	}

}
