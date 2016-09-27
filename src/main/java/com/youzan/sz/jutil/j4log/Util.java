package com.youzan.sz.jutil.j4log;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * j4log内部实现类，禁止外部使用
 */
final class Util {
	// /**
	// * @deprecated
	// * @see Logger.Level
	// */
	// public static final int LEVEL_DEBUG = 0;
	// /**
	// * @Deprecated
	// * @see Logger.Level
	// */
	// public static final int LEVEL_INFO = 1;
	// /**
	// * @deprecated
	// * @see Logger.Level
	// */
	// public static final int LEVEL_WARN = 2;
	// /**
	// * @deprecated
	// * @see Logger.Level
	// */
	// public static final int LEVEL_ERROR = 3;
	// /**
	// * @deprecated
	// * @see Logger.Level
	// */
	// public static final int LEVEL_FATAL = 4;
	//
	// /**
	// * @deprecated
	// * @see Logger.Level#name()
	// */
	// public static String[] levelStrArr = { "DEBUG", "INFO", "WARN", "ERROR",
	// "FATAL" };
	//    
	// /**
	// * 根据level的名字，如DEBUG等，获取其内部的index
	// * @param levelStr
	// * @return
	// * @deprecated level封装后，这个方法没用了
	// */
	// public static final int getLevelByStr(String levelStr) {
	// if (levelStr == null) {
	// return -1;
	// }
	//
	// for (int i = 0; i < levelStrArr.length; i++) {
	// if (levelStr.equalsIgnoreCase(levelStrArr[i])) {
	// return i;
	// }
	// }
	//
	// return -1;
	// }

	/**
	 * 根据时间，获取现时的日期的字符串，如2006-02-21
	 * 
	 * @param time
	 * @return
	 */
	public static final String getDateSimpleInfo(long time) {
		Date date = new Date(time);
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		int month = 1 + ca.get(Calendar.MONTH);
		String monthStr = String.valueOf(month);
		if (month < 10) {
			monthStr = "0" + monthStr;
		}
		int day = ca.get(Calendar.DAY_OF_MONTH);
		String dayStr = String.valueOf(day);
		if (day < 10) {
			dayStr = "0" + dayStr;
		}
		String result = ca.get(Calendar.YEAR) + "-" + monthStr + "-" + dayStr;
		return result;
	}

	/**
	 * 根据时间，获取现时的时间的字符串，如2006-02-21 17:49:43.156
	 * 
	 * @param time
	 * @return
	 */
	public static final String getDateAllInfo(long time) {
		Date date = new Date(time);
		Calendar ca = Calendar.getInstance();
		ca.setTime(date);
		int month = 1 + ca.get(Calendar.MONTH);
		String monthStr = String.valueOf(month);
		if (month < 10) {
			monthStr = "0" + monthStr;
		}
		int day = ca.get(Calendar.DAY_OF_MONTH);
		String dayStr = String.valueOf(day);
		if (day < 10) {
			dayStr = "0" + dayStr;
		}
		int hour = ca.get(Calendar.HOUR_OF_DAY);
		String hourStr = String.valueOf(hour);
		if (hour < 10) {
			hourStr = "0" + hourStr;
		}
		int minute = ca.get(Calendar.MINUTE);
		String minuteStr = String.valueOf(minute);
		if (minute < 10) {
			minuteStr = "0" + minuteStr;
		}
		int second = ca.get(Calendar.SECOND);
		String secondStr = String.valueOf(second);
		if (second < 10) {
			secondStr = "0" + second;
		}

		StringBuilder strBuf = new StringBuilder();
		strBuf.append(ca.get(Calendar.YEAR)).append("-").append(monthStr).append("-").append(dayStr);
		strBuf.append(" ").append(hourStr).append(":").append(minuteStr).append(":").append(secondStr).append(".").append(ca.get(Calendar.MILLISECOND));
		return strBuf.toString();
	}

	/**
	 * 支持变量文件名，例如${logRoot}/abc
	 * 
	 * @param path
	 * @return
	 */
	public static String getFilePath(String path) {
		if (path == null)
			return "";
		String r = "\\$\\{[^${}]*\\}";
		Pattern p = Pattern.compile(r);
		Matcher m = p.matcher(path);
		while (m.find()) {
			int startIndex = m.start(); // index of start
			int endIndex = m.end(); // index of end + 1
			String currentMatch = path.substring(startIndex, endIndex);
			String property = System.getProperty(path.substring(startIndex + 2, endIndex - 1).trim());
//			if(LoggerFactory.debug)
			System.out.println("|j4log|DEBUG|read java system property :" + currentMatch + "=" + property);
			if (property == null) {
				return "";
			}
			path = replaceAll(path, currentMatch, property);
			m = p.matcher(path);
		}
		return path;
	}

	/**
	 * 字符串全量替换
	 * 
	 * @param s
	 *            原始字符串
	 * @param src
	 *            要替换的字符串
	 * @param dest
	 *            替换目标
	 * @return 结果
	 */
	private static String replaceAll(String s, String src, String dest) {
		if (s == null || src == null || dest == null || src.length() == 0)
			return s;
		int pos = s.indexOf(src); // 查找第一个替换的位置
		if (pos < 0)
			return s;
		int capacity = dest.length() > src.length() ? s.length() * 2 : s.length();
		StringBuilder sb = new StringBuilder(capacity);
		int writen = 0;
		for (; pos >= 0;) {
			sb.append(s, writen, pos); // append 原字符串不需替换部分
			sb.append(dest); // append 新字符串
			writen = pos + src.length(); // 忽略原字符串需要替换部分
			pos = s.indexOf(src, writen); // 查找下一个替换位置
		}
		sb.append(s, writen, s.length()); // 替换剩下的原字符串
		return sb.toString();
	}
	
	/**
	 * Returns <tt>true</tt> if, and only if, <code>s.trim().length()>0<code>.
	 * 
	 * @see String#isEmpty()
	 * @author isaacdong
	 */
	public static boolean isEmpty(String s) {
		if (s == null)
			return true;
		return s.trim().isEmpty();
	}
	
	/**
	 *@see String#trim()
	 */
	public static String trim(String s) {
		if (s == null)
			return null;
		return s.trim();
	}
}
