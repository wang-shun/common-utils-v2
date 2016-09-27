package com.youzan.sz.jutil.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.youzan.sz.jutil.j4log.Logger;

/**
 * 3DES加解密的工具类
 * @author kevinxu
 *
 */
public class DESCoding
{
    private static Logger logger = Logger.getLogger("jutil");
    
    private static final String IV = "s(2L@f!o";
    
    private String fullAlgorithm = "DESede"; // 完整的 加密算法，默认为DESede
    private String algorithm = "DESede"; // 加密算法，默认为DESede
    private String mode = "ECB"; // 加密模式，默认ECB
    private byte[] keyBytes = null;
    
    /**
     * 用默认的算法DESede，传入密钥，生成工具类
     * @param keyBytes     密钥，必须是24字节
     * @throws Exception
     */
    public DESCoding(byte[] keyBytes) throws Exception
    {
        if(keyBytes.length != 24)
        {
            throw new Exception("the keys's length must be 24!");
        }
        this.keyBytes = keyBytes;
    }
    
    /**
     * 用指定的算法，传入加密的key，生成工具类
     * @param keyBytes     密钥，必须是24字节
     * @param fullAlgorithm    算法
     * @throws Exception
     */
    public DESCoding(byte[] keyBytes, String fullAlgorithm) throws Exception
    {
        if(keyBytes.length != 24)
        {
            throw new Exception("the keys's length must be 24!");
        }
        this.keyBytes = keyBytes;
        this.fullAlgorithm = fullAlgorithm;
        this.algorithm = fullAlgorithm;
        
        // 处理这种形式的fullAlgorithm：DESede/CBC/PKCS5Padding
        int p = fullAlgorithm.indexOf('/');
        if(p > 0)
        {
            algorithm = fullAlgorithm.substring(0, p);
            int q = fullAlgorithm.indexOf('/', p + 1);
            if(q > 0)
            {
                mode = fullAlgorithm.substring(p + 1, q);
            }
        }
    }    
    
    /**
     * 对指定的字节数组进行加密
     * @param src   需要进行加密的字节数组
     * @return byte[]     加密后的字节数组，若加密失败，则返回null
     */    
    public byte[] encode(byte[] src) 
    {
        try 
        {
            return process(src, Cipher.ENCRYPT_MODE);
        }
        catch (NoSuchAlgorithmException e1)
        {
            logger.warn("DESCoding.encode error in java.security.NoSuchAlgorithmException:" + e1);
            return null;
        }
        catch (NoSuchPaddingException e2)
        {
            logger.warn("DESCoding.encode error in javax.crypto.NoSuchPaddingException:" + e2);
            return null;
        }
        catch (Exception e3)
        {
            logger.warn("DESCoding.encode error in Exception:" + e3);
            return null;
        }
    }
    
    /**
     * 加密并转换成hex Str
     * @param src
     * @return String
     */
    public String encode2HexStr(byte[] src)
    {
        return HexUtil.bytes2HexStr(encode(src));
    }
    
    /**
     * 加密并转换成BASE64编码的字符串
     * @param src
     * @return String
     */
    public String encode2Base64(byte[] src)
    {
        return BASE64Coding.encode(encode(src));
    }

    /**
     * 解密
     * @param src   用3DES加密后的字节数组
     * @return byte[]     解密后的字节数组
     */
    public byte[] decode(byte[] src) 
    { 
        try 
        {
            return process(src, Cipher.DECRYPT_MODE);
        }
        catch (NoSuchAlgorithmException e1)
        {
            logger.warn("DESCoding.decode error in java.security.NoSuchAlgorithmException:" + e1);
            return null;
        }
        catch (NoSuchPaddingException e2)
        {
            logger.warn("DESCoding.decode error in javax.crypto.NoSuchPaddingException:" + e2);
            return null;
        }
        catch (Exception e3)
        {
            logger.warn("DESCoding.decode error in Exception:" + e3);
            return null;
        }
    }    

    private byte[] process(byte[] src, int type) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
    {
        //  生成密钥
        SecretKey deskey = new SecretKeySpec(this.keyBytes, algorithm);

        //  加密或解密
        Cipher c1 = Cipher.getInstance(fullAlgorithm);
        if(mode.equalsIgnoreCase("ECB"))
        {
            c1.init(type, deskey);
        }
        else
        {
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
            c1.init(type, deskey, iv);
        }
        return c1.doFinal(src);
    }
    
    
    // DES,DESede,Blowfish    
    public static void main(String[] args) throws Exception
    {
        DESCoding des = null;
        byte[] b = null;
        
        for (int i = 0; i < 1000; i++)
        {
            des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes(), "DESede/OFB/PKCS5Padding");
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
//            b = des.decode(b);            
        }
        
        long t1 = System.currentTimeMillis();
        des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes(), "DESede/OFB/PKCS5Padding");
        for (int i = 0; i < 10000; i++)
        {
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
            b = des.decode(b);            
        }
        long t2 = System.currentTimeMillis();
        System.out.println(t2 - t1);
        
        des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes(), "DESede/CBC/PKCS5Padding");
        for (int i = 0; i < 10000; i++)
        {
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
            b = des.decode(b);           
        }
        long t3 = System.currentTimeMillis();
        System.out.println(t3 - t2);
        
        des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes(), "DESede/ECB/PKCS5Padding");
        for (int i = 0; i < 10000; i++)
        {
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
            b = des.decode(b);            
        }
        long t4 = System.currentTimeMillis();
        System.out.println(t4 - t3);

        des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes());
        for (int i = 0; i < 10000; i++)
        {
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
            b = des.decode(b);            
        }
        long t5 = System.currentTimeMillis();
        System.out.println(t5 - t4);

        des = new DESCoding("%$#(*N@MHGPL><NRMvMghsO*".getBytes(), "DESede/CFB/PKCS5Padding");
        for (int i = 0; i < 10000; i++)
        {
            b = des.encode("12345asdf678asdfasdfasdfasdfasdfasdfasdfa12345asdf678asdfasdfasdfasdfasdfasdfasdfa".getBytes());
            b = des.decode(b);   
        }
        long t6 = System.currentTimeMillis();
        System.out.println(t6 - t5);
        System.exit(0);
    }
}
