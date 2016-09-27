package com.youzan.sz.jutil.net.cap;

/**
 * CapRejectedExecutionHandler的缺省实现是什么都不干
 * 
 * @author kenwaychen
 * 
 */
public class DefaultCapRejectedExecutionHandler implements CapRejectedExecutionHandler {

	@Override
	public void handle(CapSession capSession, CapPacket pack) {
		// do nothing
	}

}
