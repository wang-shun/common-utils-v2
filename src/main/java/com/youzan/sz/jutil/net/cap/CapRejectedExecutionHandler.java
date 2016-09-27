package com.youzan.sz.jutil.net.cap;

/**
 * 当框架线程池满的时候用当前selector线程回调业务的方法
 * 
 * @author kenwaychen
 * 
 */
public interface CapRejectedExecutionHandler {

	void handle(CapSession capSession, CapPacket pack);

}
