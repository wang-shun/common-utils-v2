package com.youzan.sz.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pan on 16/6/3.
 */
public class MD5 {

    private static String Md5(String sourceStr) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            String result = buf.toString();
            return result;

        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return null;
    }
    public static String get16Md5(String soruceStr){
        return Md5(soruceStr).substring(8, 24);
    }
    public static String get32Md5(String soruceStr){
        return Md5(soruceStr);
    }

}
