package com.youzan.sz.jutil.j4log.converter;

//import com.qq.jutil.j4log.LoggingEvent;

import com.youzan.sz.jutil.j4log.LoggingEvent;

public class CallerDataConverter extends Converter {

	@Override
	public String convert(LoggingEvent le) {
		StringBuffer buf = new StringBuffer();
		StackTraceElement[] cda = le.getCallerData();
		if (cda != null && cda.length > 0) {
			int limit = cda.length;

			for (int i = 0; i < limit; i++) {
				buf.append("Caller+");
				buf.append(i);
				buf.append("\t at ");
				buf.append(cda[i]);
				buf.append(LINE_SEPARATOR);
			}
			return buf.toString();
		} else {
			return CALLER_DATA_NA;
		}
	}

}
