package com.youzan.sz.jutil.crypto;

import java.io.UnsupportedEncodingException;

/**
 * RFC4648:
 * 
 * The encoding process represents 24-bit groups of input bits as output strings
 * of 4 encoded characters. Proceeding from left to right, a 24-bit input group
 * is formed by concatenating 3 8-bit input groups. These 24 bits are then
 * treated as 4 concatenated 6-bit groups, each of which is translated into a
 * single character in the base 64 alphabet.
 * 
 * Each 6-bit group is used as an index into an array of 64 printable
 * characters. The character referenced by the index is placed in the output
 * string.
 * 
 * 参考base，稍做修改，把 + / = 分别换成 - _ . 
 */
public class Base64Ext {
	private static char[] base64EncodeChars = new char[] { 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '-', '_' };

	private static byte[] base64DecodeChars = new byte[] { -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59,
			60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
			10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1,
			-1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
			38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1,
			-1, -1 };

	private Base64Ext() {
	}

	public static String encode(byte[] data) {
		try {
			int group = data.length / 3;
			int pad = data.length % 3;

			char[] encoded = new char[4 * group + (pad == 0 ? 0 : 4)];
			int idx = 0;

			for (int i = 0; i < group; i++) {
				int j = 3 * i;
				int chunk = ((data[j] & 0xff) << 16)
						| ((data[j + 1] & 0xff) << 8) | (data[j + 2] & 0xff);
				encoded[idx++] = base64EncodeChars[chunk >>> 18];
				encoded[idx++] = base64EncodeChars[(chunk >>> 12) & 0x3f];
				encoded[idx++] = base64EncodeChars[(chunk >>> 6) & 0x3f];
				encoded[idx++] = base64EncodeChars[chunk & 0x3f];
			}

			if (pad == 0) {
			} else if (pad == 1) {
				int j = 3 * group;
				encoded[idx++] = base64EncodeChars[(data[j] & 0xff) >>> 2];
				encoded[idx++] = base64EncodeChars[(data[j] & 0x03) << 4];
				encoded[idx++] = '.';
				encoded[idx++] = '.';
			} else if (pad == 2) {
				int j = 3 * group;
				encoded[idx++] = base64EncodeChars[(data[j] & 0xff) >>> 2];
				encoded[idx++] = base64EncodeChars[((data[j] & 0x03) << 4)
						| ((data[j + 1] & 0xf0) >>> 4)];
				encoded[idx++] = base64EncodeChars[(data[j + 1] & 0x0f) << 2];
				encoded[idx++] = '.';
			}

			return new String(encoded);
		} catch (RuntimeException re) {
			return null;
		}
	}

	public static byte[] decode(String data) {
		try {
			char[] chars = data.toCharArray();
			// data后面没有=号, 解码长度为data.length / 4 * 3
			// data后面只有一个=, 解码长度为data.length / 4 * 3 - 1
			// data后面有两个=, 解码长度为data.length / 4 * 3 - 2
			int pad = chars[chars.length - 2] == '.' ? 2
					: chars[chars.length - 1] == '.' ? 1 : 0;
			int group = chars.length / 4;

			byte[] decoded = new byte[group * 3 - pad];
			int idx = 0;

			for (int i = 0; i < group - 1; i++) {
				int j = 4 * i;
				byte b1 = base64DecodeChars[chars[j]];
				byte b2 = base64DecodeChars[chars[j + 1]];
				byte b3 = base64DecodeChars[chars[j + 2]];
				byte b4 = base64DecodeChars[chars[j + 3]];
				decoded[idx++] = (byte) ((b1 << 2) | ((b2 & 0x30) >>> 4));
				decoded[idx++] = (byte) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2));
				decoded[idx++] = (byte) (((b3 & 0x03) << 6) | b4);
			}
			// last group
			int j = 4 * (group - 1);
			byte b1 = base64DecodeChars[chars[j]];
			byte b2 = base64DecodeChars[chars[j + 1]];
			decoded[idx++] = (byte) ((b1 << 2) | ((b2 & 0x30) >>> 4));
			if (pad < 2) {
				byte b3 = base64DecodeChars[chars[j + 2]];
				decoded[idx++] = (byte) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2));
				if (pad < 1) {
					byte b4 = base64DecodeChars[chars[j + 3]];
					decoded[idx++] = (byte) (((b3 & 0x03) << 6) | b4);
				}
			}

			return decoded;
		} catch (RuntimeException re) {
			return null;
		}
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		System.out.println(Base64Ext.encode("我是中国人！哈哈，你个小日本！工作上不喜欢rtx或者qq这样的沟通方式，还是电话效率高。".getBytes()));
		System.out
				.println(new String(
						Base64Ext
								.decode("5oiR5piv5Lit5Zu95Lq677yB5ZOI5ZOI77yM5L2g5Liq5bCP5pel5pys77yB5bel5L2c5LiK5LiN5Zac5qyicnR45oiW6ICFcXHov5nmoLfnmoTmsp_pgJrmlrnlvI_vvIzov5jmmK_nlLXor53mlYjnjofpq5jjgII."),
						"UTF-8"));
	}

}
