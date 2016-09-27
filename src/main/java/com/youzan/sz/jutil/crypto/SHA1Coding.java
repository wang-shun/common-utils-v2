package com.youzan.sz.jutil.crypto;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.youzan.sz.jutil.j4log.Logger;


public class SHA1Coding
{
    private static Logger logger = Logger.getLogger("jutil");
    
    /**
     * 用SHA1算法加密字节数组
     * @param bytes  要加密的字节
     * @return byte[] 加密后的字节数组，若加密失败，则返回null
     */
    public static byte[] encode(byte[] bytes) 
    {
        try
        {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1"); 
            digest.update(bytes);
            byte[] digesta = digest.digest();
            return digesta;
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    /**
     * 用SHA1算法加密后再转换成hex String
     * @param bytes
     * @return String
     */
    public static String encode2HexStr(byte[] bytes)
    {
        return HexUtil.bytes2HexStr(encode(bytes));
    }
    
    /**
     * 用SHA1算法加密后再转换成BASE64编码的字符串
     * @param bytes
     * @return String
     */
    public static String encode2Base64(byte[] bytes)
    {
        return BASE64Coding.encode(encode(bytes));
    }

    /**
     * 计算文件的SHA1
     * @param filePath 文件路径
     * @return SHA1结果，若加密失败，则返回null
     */
    public static byte[] encodeFile(String filePath)
    {
        try
        {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1"); 
            FileInputStream fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            byte[] digesta = null;
            int readed = -1;
            try
            {
                while((readed = fis.read(buffer)) != -1)
                {
                    digest.update(buffer, 0, readed);
                }
                digesta = digest.digest();
            }
            catch (IOException e)
            {
                logger.error("IOException:" + filePath, e);
            }
            finally
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    logger.error("IOException:" + filePath, e);
                }
            }
            return digesta;
        }
        catch (FileNotFoundException e)
        {
            logger.error("file not found:" + filePath, e);
            return null;
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.error("NoSuchAlgorithmException:MD5", e);
            return null;
        }
    }

    /**
     * 计算文件的SHA1,转换成hex String
     * @param filePath 文件路径
     * @return SHA1结果，若加密失败，则返回null
     */
    public static String encodeFile2HexStr(String filePath)
    {
        return HexUtil.bytes2HexStr(encodeFile(filePath));
    }

    /**
     * 计算文件的SHA1,转换成Base64 string
     * @param filePath 文件路径
     * @return SHA1结果，若加密失败，则返回null
     */
    public static String encodeFile2Base64(String filePath)
    {
        byte[] bytes = encodeFile(filePath);
        if(bytes == null)
        {
            return null;
        }
        return BASE64Coding.encode(bytes);
    }
    
    public static void main(String[] args)
	{
		String s = "13642121251364813742ziwuliao";
		System.err.println(encode2HexStr(s.getBytes()));
	}
}
