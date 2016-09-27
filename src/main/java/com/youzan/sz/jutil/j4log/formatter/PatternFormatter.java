package com.youzan.sz.jutil.j4log.formatter;

import com.youzan.sz.jutil.j4log.LoggingEvent;
import com.youzan.sz.jutil.j4log.converter.*;

import java.util.ArrayList;
import java.util.List;

//import com.qq.jutil.j4log.LoggingEvent;
//import com.qq.jutil.j4log.converter.CallerDataConverter;
//import com.qq.jutil.j4log.converter.ClassOfCallerConverter;
//import com.qq.jutil.j4log.converter.Converter;
//import com.qq.jutil.j4log.converter.FileOfCallerConverter;
//import com.qq.jutil.j4log.converter.LineOfCallerConverter;
//import com.qq.jutil.j4log.converter.LineSeparatorConverter;
//import com.qq.jutil.j4log.converter.LiteralConverter;
//import com.qq.jutil.j4log.converter.LoggerConverter;
//import com.qq.jutil.j4log.converter.MessageConverter;
//import com.qq.jutil.j4log.converter.MethodOfCallerConverter;

public class PatternFormatter {
	public static final String PATTERN_MARKER = "%";
	private static final String PATTERN_LOGGER = "logger";
	private static final String PATTERN_CLASS = "class";
	private static final String PATTERN_FILE = "file";
	private static final String PATTERN_CALLER = "caller";
	private static final String PATTERN_LINE = "line";
	private static final String PATTERN_MESSAGE = "message";
	private static final String PATTERN_METHOD = "method";
	private static final String PATTERN_THREAD = "thread";
	private static final String PATTERN_N = "n";

	private String pattern;;
	private List<Converter> converters;

	public PatternFormatter() {
		this.pattern = "%message";
		this.compile();
	}

	public PatternFormatter(String pattern) {
		this.pattern = pattern;
		this.compile();
	}

	private void compile() {
		String[] parts = pattern.split(PATTERN_MARKER);

		converters = new ArrayList<Converter>();
		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				converters.add(new LiteralConverter(parts[i]));
				continue;
			}

			if (parts[i].startsWith(PATTERN_LOGGER)) {
				converters.add(new LoggerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_LOGGER.length())));

			} else if (parts[i].startsWith(PATTERN_CLASS)) {
				converters.add(new ClassOfCallerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_CLASS.length())));

			} else if (parts[i].startsWith(PATTERN_FILE)) {
				converters.add(new FileOfCallerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_FILE.length())));

			} else if (parts[i].startsWith(PATTERN_CALLER)) {
				converters.add(new CallerDataConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_CALLER.length())));

			} else if (parts[i].startsWith(PATTERN_LINE)) {
				converters.add(new LineOfCallerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_LINE.length())));

			} else if (parts[i].startsWith(PATTERN_MESSAGE)) {
				converters.add(new MessageConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_MESSAGE.length())));

			} else if (parts[i].startsWith(PATTERN_METHOD)) {
				converters.add(new MethodOfCallerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_METHOD.length())));

			} else if (parts[i].startsWith(PATTERN_THREAD)) {
				converters.add(new MethodOfCallerConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_THREAD.length())));

			} else if (parts[i].startsWith(PATTERN_N)) {
				converters.add(new LineSeparatorConverter());
				converters.add(new LiteralConverter(parts[i]
						.substring(PATTERN_N.length())));

			} else {
				converters.add(new LiteralConverter(PATTERN_MARKER + parts[i]));
			}
		}
	}

	public String format(LoggingEvent le) {
		StringBuilder buf = new StringBuilder(128);
		for (Converter c : converters) {
			c.write(buf, le);
		}
		return buf.toString();
	}

	@Override
	public String toString()
	{
	    return "PatternFormatter [pattern=" + pattern + "]";
	}	
}
