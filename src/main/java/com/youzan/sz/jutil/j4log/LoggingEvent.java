package com.youzan.sz.jutil.j4log;

//import com.qq.jutil.j4log.helper.FormattingTuple;
//import com.qq.jutil.j4log.helper.MessageFormatter;
//import com.qq.jutil.j4log.helper.CallerData;

import com.youzan.sz.jutil.j4log.helper.CallerData;
import com.youzan.sz.jutil.j4log.helper.FormattingTuple;
import com.youzan.sz.jutil.j4log.helper.MessageFormatter;

public class LoggingEvent {

	private String loggerName;

	private String loggerFullName;
	
	private String message;

	private transient Object[] argumentArray;

	private transient String formattedMessage;

	private StackTraceElement[] callerDataArray;

	private String threadName;

	public LoggingEvent() {
	}

	public LoggingEvent(String logName, String message, Object[] argArray) {
		this.loggerName = logName;
		this.loggerFullName = Logger.FQN;
		this.message = message;
		this.argumentArray = argArray;
		FormattingTuple ft = MessageFormatter.arrayFormat(message, argArray);
		this.formattedMessage = ft.getMessage();
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		if (this.message != null) {
			throw new IllegalStateException(
					"The message for this event has been set already.");
		}
		this.message = message;
	}

	public Object[] getArgumentArray() {
		return this.argumentArray;
	}

	public void setArgumentArray(Object[] argArray) {
		if (this.argumentArray != null) {
			throw new IllegalStateException("argArray has been already set");
		}
		this.argumentArray = argArray;
	}

	public String getFormattedMessage() {
		if (formattedMessage != null) {
			return formattedMessage;
		}
		if (argumentArray != null) {
			formattedMessage = MessageFormatter.arrayFormat(message,
					argumentArray).getMessage();
		} else {
			formattedMessage = message;
		}

		return formattedMessage;
	}

	public StackTraceElement[] getCallerData() {
		if (callerDataArray == null) {
			callerDataArray = CallerData.extract(new Throwable(), loggerFullName,
					Integer.MAX_VALUE, null);
		}
		return callerDataArray;
	}

	public boolean hasCallerData() {
		return (callerDataArray != null);
	}

	public void setCallerData(StackTraceElement[] callerDataArray) {
		this.callerDataArray = callerDataArray;
	}

	public String getThreadName() {
		if (threadName == null) {
			threadName = (Thread.currentThread()).getName();
		}
		return threadName;
	}

	public void setThreadName(String threadName) throws IllegalStateException {
		if (this.threadName != null) {
			throw new IllegalStateException("threadName has been already set");
		}
		this.threadName = threadName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getFormattedMessage());
		return sb.toString();
	}

}
