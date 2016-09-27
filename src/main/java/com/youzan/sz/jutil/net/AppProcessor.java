package com.youzan.sz.jutil.net;


/**
 * 详细内容见UDPServer的文档.网络和应用分离后的应用部分封装
 * @author junehuang
 *
 */
public interface AppProcessor {
	/**
	 * @param req 对网络请求的封装
	 * @return 返回给客户端的应用数据，UDPServer将会加上包头后应答给客户端。return null表示不应答本次请求（例如不需要响应的udp请求等等）
	 */
	public byte[] process(RequestObj req);
}
