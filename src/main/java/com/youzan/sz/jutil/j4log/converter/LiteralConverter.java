package com.youzan.sz.jutil.j4log.converter;

//import com.qq.jutil.j4log.LoggingEvent;

import com.youzan.sz.jutil.j4log.LoggingEvent;

public class LiteralConverter extends Converter {
	private String literal;

	public LiteralConverter(String literal) {
		this.literal = literal;
	}

	@Override
	public String convert(LoggingEvent le) {
		return this.literal;
	}

}
