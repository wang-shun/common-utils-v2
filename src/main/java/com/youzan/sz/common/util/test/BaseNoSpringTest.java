package com.youzan.sz.common.util.test;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.FileUtils;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by wangpan on 16/9/4.
 */
@RunWith(ExtendedJunit4Class.class)
public abstract class BaseNoSpringTest extends BaseTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseSpringTest.class);

    public static void initWithProfile(EnvProfile envProfile) {
        try {
            BaseSpringTest.SpringTestConfig springTestConfig = envProfile.getSpringTestConfig();
            URL classPathURL = BaseSpringTest.class.getClassLoader().getResource("");
            final String classPath = classPathURL.getFile();
            LOGGER.info("current classPath :{}", classPath);

            //删除历史配置文件(class文件不删除,IDE会自动编译更新)
            String[] dirtyFileList = new File(classPath).list((dir, name) -> !name.startsWith("com"));
            for (String dirtyFile : dirtyFileList) {
                FileUtils.deleteFile(dirtyFile);
            }

            //复制deploy配置文件
            String relativePath = "../../../" + springTestConfig.getAppSimpleName() + "-deploy/src/main/resources/";
            String oldPropertiesPath = new URL(classPathURL, relativePath).getFile();
            FileUtils.copyDirectiory(oldPropertiesPath, classPath);

            //复制当前测试类配置文件
            String oldConfigRelative = "../../src/main/resources/";
            String oldConfigPath = new URL(classPathURL, oldConfigRelative).getFile();
            FileUtils.copyDirectiory(oldConfigPath, classPath);

            //使用filter值覆盖env里面的值;
            cpProperties(classPath + "filters" + File.separator + springTestConfig.getPropertyName(),
                    classPath + ConfigsUtils.ENV_PROPERTIES_FILE_NAME);
            cpProperties(classPath + "filters" + File.separator + springTestConfig.getPropertyName(),
                    classPath + ConfigsUtils.CONTAINER_PROPERTIES_FILE_NAME);
            //移除filter文件
            String[] list = new File(classPath).list((dir, name) -> name.equalsIgnoreCase("filters"));
            for (String filterFilePath : list) {
                FileUtils.deleteFile(filterFilePath);
            }

            //删除脏文件(如果由于没有删除,会造成logback报错)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteFile(classPathURL.getFile())));
        } catch (Exception e) {
            LOGGER.error("file init fail", e);
        }

    }

    private static void cpProperties(String srcFilePath, String targetFilePath) {
        LOGGER.info("start move properties,srcPath:{},ttPath:{}", srcFilePath, targetFilePath);

        if(!new File(targetFilePath).exists()){
            return;
        }
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
     *
     * */
    interface SpringTestConfig {
        String getAppSimpleName();

        String getPropertyName();
    }

    protected static final BaseSpringTest.SpringTestConfig QA_CONFIG   = new BaseSpringTest.DefaultSpringTestConfig().setProperty("qa.properties");
    protected static final BaseSpringTest.SpringTestConfig PROD_CONFIG = new BaseSpringTest.DefaultSpringTestConfig().setProperty("prod.properties");
    protected static final BaseSpringTest.SpringTestConfig CI_CONFIG   = new BaseSpringTest.DefaultSpringTestConfig().setProperty("ci.properties");
    protected static final BaseSpringTest.SpringTestConfig DEV_CONFIG  = new BaseSpringTest.DefaultSpringTestConfig().setProperty("dev.properties");

    public static class DefaultSpringTestConfig implements BaseNoSpringTest.SpringTestConfig {
        private String defaultProfileProperty = "dev.properties";
        private String appSimpleName          = null;

        public BaseNoSpringTest.DefaultSpringTestConfig setSimpleAppName(String appSimpleName) {
            this.appSimpleName = appSimpleName;
            return this;
        }

        public BaseNoSpringTest.DefaultSpringTestConfig setProperty(String propertyFileName) {
            this.defaultProfileProperty = propertyFileName;
            return this;
        }

        @Override
        public String getPropertyName() {
            return defaultProfileProperty;
        }

        /**获得应用简称*/
        public String getAppSimpleName() {
            if (appSimpleName == null) {
                this.appSimpleName = getAppSimpleName(getClass().getClassLoader().getResource("").getFile());
            }
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
