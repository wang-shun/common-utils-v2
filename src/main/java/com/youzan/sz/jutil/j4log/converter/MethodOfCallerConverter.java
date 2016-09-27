package com.youzan.sz.jutil.j4log.converter;

//import com.qq.jutil.j4log.LoggingEvent;

import com.youzan.sz.jutil.j4log.LoggingEvent;

public class MethodOfCallerConverter extends Converter {

	@Override
	public String convert(LoggingEvent le) {
		StackTraceElement[] cda = le.getCallerData();
		if (cda != null && cda.length > 0) {
			return cda[0].getMethodName();
		} else {
			return NA;
		}
	}

}
