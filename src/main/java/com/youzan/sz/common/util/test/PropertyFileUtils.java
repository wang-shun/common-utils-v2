package com.youzan.sz.common.util.test;

import java.io.*;
import java.util.Properties;

/**
 *
 * Created by zhanguo on 16/7/27.
 */
public class PropertyFileUtils {
    /**
     * 读取配置文件键值对 
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
     * @return key对应的值
     */
    public static Properties getKeyValues(String filePath) {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            in.close();
            return props;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 修改或添加键值对 如果key存在，修改, 反之，添加。 
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
     * @param key 键 
     * @param value 键对应的值 
     */
    public static void update(String filePath, String key, String value) {
        //获取绝对路径  
        Properties prop = new Properties();
        try {
            File file = new File(filePath);
            if (!file.exists())
                file.createNewFile();
            InputStream fis = new FileInputStream(file);
            prop.load(fis);
            //一定要在修改值之前关闭fis  
            fis.close();
            OutputStream fos = new FileOutputStream(filePath);
            prop.setProperty(key, value);
            //保存，并加入注释  
            prop.store(fos, "Update '" + key + "' value");
            fos.close();
        } catch (IOException e) {
            System.err.println("Visit " + filePath + " for updating " + value + " value error");
        }
    }

}
