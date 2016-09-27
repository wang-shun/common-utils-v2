package com.youzan.sz.jutil.j4log.converter;

//import com.qq.jutil.j4log.LoggingEvent;

import com.youzan.sz.jutil.j4log.LoggingEvent;

abstract public class Converter {
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static final String NA = "?";
	public static final String CALLER_DATA_NA = "?#?:?" + LINE_SEPARATOR;

	public abstract String convert(LoggingEvent event);

	public void write(StringBuilder buf, LoggingEvent event) {
		buf.append(convert(event));
	}
}
