package com.youzan.sz.jutil.j4log.converter;

//import com.qq.jutil.j4log.LoggingEvent;

import com.youzan.sz.jutil.j4log.LoggingEvent;

public class LineSeparatorConverter extends Converter {

	@Override
	public String convert(LoggingEvent le) {
		return LINE_SEPARATOR;
	}

}
