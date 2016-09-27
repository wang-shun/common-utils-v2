package com.youzan.sz.jutil.net.simple;
/**
 * SessionHandler被线程池回调，现在只在接收到网络包的时候触发，以后可能扩展接口（超时，销毁，真正发送后触发等等）
 * 你可以将SessionHandler想象成一个servlet，被resin的线程池回调。
 * @author junehuang
 *
 */
public interface SessionHandler {
	public void packetReceived(Session session, Packet packet);
}
