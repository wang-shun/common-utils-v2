package com.youzan.sz.jutil.j4log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
/**
 * @deprecated
 * @author isaacdong
 *
 */
public final class Configure {
	private static final String LOG_CONF = "j4log.property";

	/**
	 * 已经不工作了，仅仅只是为了兼容老的方法定义
	 * 
	 * @return
	 * @see Logger#getLoggerMap()
	 * @deprecated
	 */
	public static HashMap<String, Logger> getLoggerMap() {
		return new HashMap<String, Logger>(0);
	}

	/**
	 * 没有用处了，仅仅只是为了兼容老的方法定义
	 * 
	 * @return
	 * @deprecated
	 * @see Logger#getLogger(String)
	 */
	public static Logger getLogger(String name) {
		return Logger.getLogger(name);
	}

	/**
	 * 获取全局的logger的数组，仅仅为了保持兼容性，还保留了,但是已经不工作了,取不到东西了。
	 * 
	 * @return Logger[]
	 * @deprecated
	 */
	public static Logger[] getLoggerArr() {
		return new Logger[0];
		// Collection<Logger> coll = Logger.getLoggerMap().values();
		// Logger[] array = new Logger[coll.size()];
		// int i = 0;
		// for( Logger log : coll ){
		// array[i++] = log;
		// }
		// return array;
	}

	private static boolean testFile(String filePath) {
		if (filePath == null || filePath.trim().equals("")) {
			return false;
		}
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filePath + "." + Util.getDateSimpleInfo(System.currentTimeMillis()), true);
		} catch (FileNotFoundException e) {
			return false;
		} finally {
			try {
				fout.close();
			} catch (Exception e) {
			}
		}
		return true;
	}

	/**
	 * @deprecated
	 */
	public static void init() {
	}

	/**
	 * 读配置文件初始化j4log
	 * 
	 */
//	static void initate() {
//		try {
//			Properties prop = new Properties();
//			// prop.load(new
//			// FileInputStream("/usr/local/tomcat/webapps/waptest/WEB-INF/classes/j4log.property"));
//			// InputStream in =
//			// Configure.class.getResourceAsStream("/j4log.property");
//			InputStream in = CongfigResource.loadConfigFile(LOG_CONF, Configure.class);
//			prop.load(in);
//			// System.out.println(prop.toString());
//			Set<Map.Entry<Object, Object>> entrySet = prop.entrySet();
//			Iterator<Map.Entry<Object, Object>> it = entrySet.iterator();
//			while (it.hasNext()) {
//				try {
//					Map.Entry<Object, Object> entry = it.next();
//					String loggerName = ((String) entry.getKey()).trim();
//					String valueStr = ((String) entry.getValue()).trim();
//					String[] arr = StringUtil.split(valueStr, ",");
//					// TODO 这行会抛异常的... 需要处理一下
//					Level level = Level.valueOf(arr[0].trim().toUpperCase());
//					if (arr.length == 2) { // 本地log
//						String filePath = Tools.getFilePath(arr[1].trim());
//						// 先测试一下该文件是否配置有错，若有错，则打warnning，并不加进logger列表中
//						if (!testFile(filePath)) {
//							System.err.println("!! the configure of " + loggerName + " is wrong! log path=[" + filePath + "]");
//							continue;
//						}
//
//						Logger logger = Logger.getLogger(loggerName);
//						logger.initFileLog(level, filePath);
//					} else if (arr.length > 3) { // 远程log
//						String type = arr[1].trim();
//						String[] s = StringUtil.split(arr[2], ":");
//						InetAddress addr = AddressUtil.ip2Addr(s[0].trim());
//						if (addr == null) {
//							System.err.println("!! the configure of " + loggerName + " is wrong! remote ip=[" + s[0] + "]");
//							continue;
//						}
//						int port = StringUtil.convertInt(s.length > 1 ? s[1] : null, 60021);
//						String failLog = Tools.getFilePath(arr[3].trim());
//						if (failLog == null || failLog.equals("")) {
//							failLog = null;
//							System.err.println("!! the configure of " + loggerName + " warning: failLog is empty.");
//						} else if (!testFile(failLog)) {
//							System.err.println("!! the configure of " + loggerName + " is wrong! log path=[" + failLog + "]");
//							continue;
//						}
//
//						String localLog = arr.length > 4 ? Tools.getFilePath(arr[4].trim()) : null;
//						if (localLog != null && !testFile(localLog)) {
//							System.err.println("!! the configure of " + loggerName + " is wrong! log path=[" + localLog + "]");
//							continue;
//						}
//						Logger logger = Logger.getLogger(loggerName);
//						logger.initLogClient(level, type, addr, port, localLog, failLog);
//					} else {
//						System.err.println("!! the configure of " + loggerName + " is wrong: " + valueStr);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//
//			Thread th = new Thread(new LogWorkThread(), "logger_thread");
//			th.start();
//			Thread thRemote = new Thread(new LogWorkThread(true), "logger_thread");
//			thRemote.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}
