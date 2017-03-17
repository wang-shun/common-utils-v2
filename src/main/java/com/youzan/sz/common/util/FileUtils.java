package com.youzan.sz.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Properties;

/**
 * Created by zhanguo on 16/7/27.
 */
public class FileUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    // 复制文件 
    public static void copyFile(File sourceFile, File targetFile) {
        // 新建文件输入流并对它进行缓冲
        try (FileInputStream input = new FileInputStream(sourceFile);
             BufferedInputStream inBuff = new BufferedInputStream(input);
             // 新建文件输出流并对它进行缓冲
             FileOutputStream output = new FileOutputStream(targetFile);
             BufferedOutputStream outBuff = new BufferedOutputStream(output)) {

            // 缓冲数组 
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流 
            outBuff.flush();

            //关闭流 
            inBuff.close();
            outBuff.close();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 复制文件夹
    public static void mergeDirectory(String sourceDir, String targetDir) throws Exception {


        File[] commonFiles = (new File(sourceDir)).listFiles();
        if (commonFiles == null || commonFiles.length == 0) {
            return;
        }
        for (File file : commonFiles) {

            if (file.isFile()) {

                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file.getName());

                Properties properties = new Properties();

                meregeProperties(properties, file);

                if (targetFile.exists()) {

                    meregeProperties(properties, targetFile);
                }
                storeProperties(properties, targetFile);

            } else {
                // 准备复制的源文件夹
                String dir1 = sourceDir + "/" + file.getName();
                // 准备复制的目标文件夹
                String dir2 = targetDir + "/" + file.getName();
                mergeDirectory(dir1, dir2);

            }
        }

    }

    protected static void meregeProperties(Properties properties, File source) throws Exception {
        InputStream is = null;
        try {
            is = new FileInputStream(source);
            Reader r = new InputStreamReader(is, StandardCharsets.UTF_8.displayName());
            Properties p = new Properties();
            p.load(r);
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                properties.setProperty(key, value);

            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (is != null) {
                is.close();
            }
        }

    }

    protected static void storeProperties(Properties properties, File file) throws Exception {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            properties.store(os, null);
        } catch (Exception e) {
            throw e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw e;
                }
            }
        }
    }

    // 复制文件夹
    public static void copyDirectiory(String sourceDir, String targetDir) {
        // 新建目标目录 
        (new File(targetDir)).mkdirs();
        // 获取源文件夹当前下的文件或目录 
        File[] file = (new File(sourceDir)).listFiles();
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile()) {
                // 源文件 
                File sourceFile = file[i];
                // 目标文件 
                File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
                if (targetFile.exists()) {
                    continue;
                }
                copyFile(sourceFile, targetFile);
            }
            if (file[i].isDirectory()) {
                // 准备复制的源文件夹 
                String dir1 = sourceDir + "/" + file[i].getName();
                // 准备复制的目标文件夹 
                String dir2 = targetDir + "/" + file[i].getName();
                copyDirectiory(dir1, dir2);
            }
        }
    }

    public static void deleteFile(String filePath) {
        if (!new File(filePath).exists()) {
            LOGGER.info("file:{} is empty,skip delete", filePath);
            return;
        }

        Path directory = Paths.get(filePath);
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            LOGGER.error("删除文件:{}失败", filePath, e);
        }
        LOGGER.info("delete file:{}", filePath);

    }

}
