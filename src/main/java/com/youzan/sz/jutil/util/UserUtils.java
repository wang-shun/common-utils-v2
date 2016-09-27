package com.youzan.sz.jutil.util;

/**
 * 判断手机腾讯网的用户类型的工具类
 * 
 * @author kenwaychen
 * 
 */
public class UserUtils {

	/**
	 * 手机腾讯网的用户类型，根据QQ号分为3种：有效的UIN，有效的游客，无效的UIN。
	 * 
	 * @author kenwaychen
	 * 
	 */
	public static enum UinType {
		VALID_UIN, VALID_GUEST, INVALID_UIN
	}

	/**
	 * 获取该QQ号对应的用户类型
	 * 
	 * @param uin 用户QQ号
	 * @return 可能返回的结果<br />
	 *         1. UinType.VALID_UIN(有效的UIN)<br />
	 *         2. UinType.VALID_GUEST(有效的游客)<br />
	 *         3. UinType.INVALID_UIN(无效的UIN)<br />
	 */
	public static UinType getUinType(long uin) {
		if (Tools.isValidQQ(uin))
			return UinType.VALID_UIN;
		else if (uin <= -10000)
			return UinType.VALID_GUEST;
		else
			return UinType.INVALID_UIN;
	}

}
