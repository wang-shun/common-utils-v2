package com.youzan.sz.jutil.string;

import java.util.Random;

/**
 * 随机工具
 * @author meteorchen
 *
 */
public final class RandomUtil {
	
	private static final Random rand = new Random();
	private static final String DEFAULT_RAND_CHAR_SET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMLOPQRSTUVWXYZ1234567890";
	
	/**
	 * 生成随机字符串
	 * @param char_set		字符串中允许出现的字符
	 * @param len			字符串的长度
	 * @return				随机字符串
	 */
	public static String getString(String char_set, int len){
        char[] chars = new char[len];
        for(int i = 0; i < len; ++i){
            int r = rand.nextInt(char_set.length());
            chars[i] = char_set.charAt(r) ;
        }
        return new String(chars);
	}
    
	/**
	 * 生成随机字符串（可能出现的字符为大小写字母和数字）
	 * @param len		要生成的字符串的长度
	 * @return			随机字符串
	 */
	public static String getString(int len){
		return getString(DEFAULT_RAND_CHAR_SET, len);
	}
	
	/**
	 * 生成只包含数字的随机字符串
	 * @param len		长度
	 * @return			结果
	 */
	public static String getIntString(int len){
		return getString("0123456789", len);
	}
	
	/**
	 * 生成随机字符串（长度为3）
	 * @return	结果
	 */
	public static String getIntString(){
		return getIntString(3);
	}
	
	public static void main(String[] args) {
        System.out.println(getString("hello",123));
	}
}