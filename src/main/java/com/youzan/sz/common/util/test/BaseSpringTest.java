package com.youzan.sz.common.util.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.youzan.sz.common.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.FileUtils;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 单元测试专用
 * 如果需要测试远程service,{@link BaseIntTest}
 *
 */

public abstract class BaseSpringTest extends BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseSpringTest.class);

    public static void initWithProfile(EnvProfile envProfile) {
        try {
            SpringTestConfig springTestConfig = envProfile.getSpringTestConfig();
            URL classPathURL = BaseSpringTest.class.getClassLoader().getResource("");
            String classPath = classPathURL.getFile();
            LOGGER.info("current classPath :{}", classPath);
            String relativePath = "../../../" + springTestConfig.getAppSimpleName() + "-deploy/src/main/resources/";
            String oldPropertiesPath = new URL(classPathURL, relativePath).getFile();
            //递归resourc文件
            FileUtils.copyDirectiory(oldPropertiesPath, classPath);

            //覆盖系统变量,这样才能找到新的配置文件
            System.setProperty("props.path", classPath);

            //复制
            String oldConfigRelative = "../../src/main/resources/";
            String oldConfigPath = new URL(classPathURL, oldConfigRelative).getFile();
            FileUtils.copyDirectiory(oldConfigPath, classPath);

            //使用filter值覆盖env里面的值;
            cpProperties(classPath + File.separator + "filters" + File.separator + springTestConfig.getPropertyName(),
                classPath + File.separator + ConfigsUtils.ENV_PROPERTIES_FILE_NAME);
            //移除filter文件

            String[] list = new File(classPath).list();
            for (String fileName : list) {
                if (fileName.equalsIgnoreCase("filters") && new File(fileName).isDirectory()) {
                    new File(fileName).deleteOnExit();
                    break;
                }
            }
            //使用配置文件的值,覆盖logback里面的引用

            replaceXML(classPath, classPath + File.separator + "logback.xml");

        } catch (MalformedURLException e) {
            LOGGER.error("file init fail", e);
        }
    }

    private static void replaceXML(String classPath, String xmlFilePath) {
        File xmlFile = new File(xmlFilePath);
        List<String> lines = null;

        try {
            lines = Files.readAllLines(xmlFile.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            LOGGER.error("", e);
        }
        if (lines != null) {
            boolean isReplace = false;
            List<String> newLines = new ArrayList<>(lines.size());
            for (String line : lines) {
                boolean isLineReplace = false;
                int startIndex = line.indexOf("${");
                if (startIndex > 0) {
                    int endIndex = line.indexOf("}", startIndex);
                    String key = line.substring(startIndex, endIndex);
                    LOGGER.info("get key:{}", key);
                    Object propertyValue = getPropertyValue(classPath, key);
                    if (propertyValue != null) {
                        line = line.replace("${" + key + "}", propertyValue.toString());
                        LOGGER.info("update key:{} to value{}", key, propertyValue);
                        newLines.add(line);
                        isLineReplace = true;
                        isReplace = true;
                    }
                }
                if (!isLineReplace) {
                    newLines.add(line);
                }

            }
            if (isReplace) {
                xmlFile.deleteOnExit();
                try {
                    Files.write(xmlFile.toPath(), lines, Charset.defaultCharset());
                } catch (IOException e) {
                    LOGGER.error("写入新的loggback失败", e);
                }
            }
        }

    }

    private static Object getPropertyValue(String classPath, String key) {
        LOGGER.info("search value of key({}) ", key);

        String[] propertyFileList = new File(classPath).list((dir, name) -> name.endsWith(".properties"));
        for (String propertyFilePath : propertyFileList) {
            Properties keyValues = PropertyFileUtils.getKeyValues(propertyFilePath);
            if (keyValues.get(key) != null) {
                return keyValues.get(key);
            }
            if (keyValues.get(key.trim()) != null) {
                return keyValues.get(key.trim());
            }

        }
        return null;
    }

    public EnvProfile getEnvProfile() {
        return getClass().getAnnotation(TestProfile.class).value();
    }

    private static void cpProperties(String srcFilePath, String targetFilePath) {
        LOGGER.info("start move properties,srcPath:{},targetPath:{}", srcFilePath, targetFilePath);

        Properties keyValues = PropertyFileUtils.getKeyValues(srcFilePath);
        if (keyValues == null) {
            return;
        }
        for (Map.Entry<Object, Object> objectObjectEntry : keyValues.entrySet()) {

            PropertyFileUtils.update(targetFilePath, objectObjectEntry.getKey().toString(),
                objectObjectEntry.getValue() == null ? "" : String.valueOf(objectObjectEntry.getValue()));

            LOGGER.info("移动:{}属性(key={},value={})到:{}", srcFilePath, objectObjectEntry.getKey(),
                objectObjectEntry.getValue(), targetFilePath);

        }
    }

    /**一般的测试场景不需要关心以下代码
     * 在测试类的无参构造器中可以调用一个实现方法,从而可以替换不同的配置文件.例如单元测试QA环境
     *
     * */
    interface SpringTestConfig {
        String getAppSimpleName();

        String getPropertyName();
    }

    protected static final SpringTestConfig QA_CONFIG   = new DefaultSpringTestConfig().setProperty("qa.properties");
    protected static final SpringTestConfig PROD_CONFIG = new DefaultSpringTestConfig().setProperty("prod.properties");
    protected static final SpringTestConfig CI_CONFIG   = new DefaultSpringTestConfig().setProperty("ci.properties");
    protected static final SpringTestConfig DEV_CONFIG  = new DefaultSpringTestConfig().setProperty("dev.properties");

    public static class DefaultSpringTestConfig implements SpringTestConfig {
        private String defaultProfileProperty = "dev.properties";
        private String appSimpleName          = getAppSimpleName(getClass().getClassLoader().getResource("").getFile());

        public DefaultSpringTestConfig setSimpleAppName(String appSimpleName) {
            this.appSimpleName = appSimpleName;
            return this;
        }

        public DefaultSpringTestConfig setProperty(String propertyFileName) {
            this.defaultProfileProperty = propertyFileName;
            return this;
        }

        @Override
        public String getPropertyName() {
            return defaultProfileProperty;
        }

        /**获得应用简称*/
        public String getAppSimpleName() {
            return appSimpleName;
        }

        private static String getAppSimpleName(String filePath) {
            String[] pathArray = filePath.split(File.separator);
            for (int i = pathArray.length - 1; i > 0; i--) {
                if (pathArray[i].startsWith(pathArray[i - 1] + "-")) {//项目模块以"-"分割
                    return pathArray[i - 1];
                }
            }
            LOGGER.error("未能识别项目名字,请覆盖获取项目名字方法");
            return null;
        }
    }
}
