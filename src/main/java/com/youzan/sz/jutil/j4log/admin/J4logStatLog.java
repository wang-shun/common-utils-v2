package com.youzan.sz.jutil.j4log.admin;

import com.youzan.sz.jutil.j4log.Logger;
import com.youzan.sz.jutil.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

//import com.qq.jutil.j4log.Logger;
//import com.qq.jutil.util.Pair;

/**
 * <b>功能：j4log初始化过程日志，主要是两个功能 1，错误的j4log配置记录 2，打印j4log使用情况统计</b><br>
 * <br>
 * <b>完整路径：</b> com.qq.jutil.j4log.admin.J4logInitLog <br>
 * <b>创建日期：</b> 2013-3-22 上午11:45:45 <br>
 * 
 * @author <a href="mailto:seonzhang@tencent.com">seonzhang(zhanggaojing)</a><br>
 *         <a href="http://www.tencent.com">Shenzhen Tencent Co.,Ltd.</a>
 * @version 1.0, 2013-3-22
 */
public class J4logStatLog
{

	private static String                                                        J4logInitiler      = "J4logInitiler";
	private static ConcurrentHashMap<String, Pair<AtomicInteger, AtomicInteger>> loggerSuccFailStat = new ConcurrentHashMap<String, Pair<AtomicInteger, AtomicInteger>>();

	enum ConfigSource
	{
		Local("local"), ConfigCenter("configcenter"), Companion("companion");
		String value;

		ConfigSource(String value)
		{
			this.value = value;
		}

		String getValue()
		{
			return this.value;
		}
	}

	// 错误j4log配置记录类
	static class ErrorMark
	{
		ConfigSource source;
		String loggerName;
		String error;
		String config;

		private ErrorMark(ConfigSource source, String loggerName, String error, String config)
		{
			super();
			this.source = source;
			this.loggerName = loggerName;
			this.error = error;
			this.config = config;
		}

		@Override
		public String toString()
		{
			return this.source.getValue() + "\t" + this.loggerName + "\t" + this.error + "\t"
					+ this.config;
		}
	}

	static ConcurrentLinkedQueue<ErrorMark> markList = new ConcurrentLinkedQueue<ErrorMark>();

	// 记录本地配置出错的logger
	public static void markLocal(String loggerName, String error, String config)
	{
		markList.offer(new ErrorMark(ConfigSource.Local, loggerName, error, config));
	}

	// 记录配置中心配置出错的logger
	public static void markConfigcenter(String loggerName, String error, String config)
	{
		markList.offer(new ErrorMark(ConfigSource.ConfigCenter, loggerName, error, config));
	}

	// 记录伴侣配置出错的logger
	public static void markCompanion(String loggerName, String error, String config)
	{
		markList.offer(new ErrorMark(ConfigSource.Companion, loggerName, error, config));
	}

	// 打印采集的信息
	public static void flush()
	{
		Logger logger = Logger.getLogger(J4logInitiler);
		while (!markList.isEmpty())
		{
			logger.info(markList.poll().toString());
		}
	}

	static AutoDiscardingQueue<LogPackage> logPackageQueue = new AutoDiscardingQueue<LogPackage>(
			1000);

	// 必须在j4log初始化完成后调用
	public static void error(String s, Throwable t)
	{
		Logger logger = Logger.getLogger(J4logInitiler);
		logger.error(s, t);
	}

	// 记录Logserver远程日志发包信息
	public static void stat(String loggerName, String ip, long sendtime, int itemCount,
			boolean isSucc)
	{
		// 最近一千条记录
		logPackageQueue.offer(new LogPackage(loggerName, ip, sendtime, itemCount, isSucc));
		// 记录成功失败
		statSuccOrFail(loggerName, isSucc);
	}

	// 远程日志的成功或失败次数统计
	private static void statSuccOrFail(String loggerName, boolean success)
	{

		Pair<AtomicInteger, AtomicInteger> pair = loggerSuccFailStat.get(loggerName);
		if (pair == null)
		{
			pair = Pair.makePair(new AtomicInteger(), new AtomicInteger());
			loggerSuccFailStat.putIfAbsent(loggerName, pair);
		}
		if (success)
		{
			pair.first.incrementAndGet();
		}
		else
		{
			pair.second.incrementAndGet();
		}
	}

	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// 输出最近1000条统计
	public static String showLatest1000()
	{
		StringBuilder info = new StringBuilder(1024);
		String title = "%-40s %-20s %-30s %-5s %-5s\n";
		info.append(String.format(title, "name", "ip", "time", "count", "result"));
		// 格式化输出
		Iterator<LogPackage> iterator = logPackageQueue.iterator();
		while (iterator.hasNext())
		{
			LogPackage logPackage = iterator.next();
			info.append(String.format(title, logPackage.getLoggerName(), logPackage.getIp(),
					simpleDateFormat.format(new Date(logPackage.getSendtime())),
					logPackage.getItemCount(),
					logPackage.isSucc ? "succ" : "fail"));
		}
		return info.toString();
	}

	// 输出远程日志成功失败 统计
	public static String showSuccAndFail()
	{
		StringBuilder info = new StringBuilder(1024);
		String title = "%-40s %-20s %-20s\n";
		info.append(String.format(title, "name", "succ", "fail"));
		Iterator<Entry<String, Pair<AtomicInteger, AtomicInteger>>> interator = loggerSuccFailStat
				.entrySet().iterator();
		while (interator.hasNext())
		{
			Entry<String, Pair<AtomicInteger, AtomicInteger>> entry = interator.next();
			info.append(String.format(title, entry.getKey(), entry.getValue().first.intValue(),
					entry.getValue().second.intValue()));
		}
		return info.toString();
	}

	static class LogPackage
	{ // 因为远程日志都是批量被发送的LogServer，所以这里也是log包
		String loggerName;// 日志名
		String ip; // logserver IP
		long sendtime; // 发包时间
		int itemCount; // 包中包含几条日志
		boolean isSucc; // 是否成功

		private LogPackage(String loggerName, String ip, long sendtime, int itemCount,
				boolean isSucc)
		{
			super();
			this.loggerName = loggerName;
			this.ip = ip;
			this.sendtime = sendtime;
			this.itemCount = itemCount;
			this.isSucc = isSucc;
		}

		public String getLoggerName()
		{
			return this.loggerName = (this.loggerName == null) ? "" : this.loggerName.trim();
		}

		public String getIp()
		{
			return this.ip = (this.ip == null) ? "" : this.ip.trim();
		}

		public long getSendtime()
		{
			return this.sendtime;
		}

		public int getItemCount()
		{
			return this.itemCount;
		}

		public boolean isSucc()
		{
			return this.isSucc;
		}

	}

}
