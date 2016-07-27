package com.youzan.sz.common.util.test;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.FileUtils;

/**
 *
 * Created by zhanguo on 16/7/27.
 * 单元测试专用
 * 如果需要测试远程service,{@link BaseIntTest}
 * 
 */

@SuppressWarnings("SpringContextConfigurationInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:config-spring.xml")
public abstract class BaseSpringTest<T extends BaseSpringTest> extends BaseTest {
    private final static Logger       LOGGER           = LoggerFactory.getLogger(BaseSpringTest.class);

    protected static SpringTestConfig springTestConfig = null;

    @BeforeClass
    public static void initClass() {
        try {
            if (springTestConfig == null) {//如果没有配置,则使用默认的
                springTestConfig = new DefaultSpringTestConfig();
            }

            URL classPathURL = BaseSpringTest.class.getClassLoader().getResource("");
            String classPath = classPathURL.getFile();
            LOGGER.info("current classPath path:{}", classPath);
            String relativePath = "../../../" + springTestConfig.getAppSimpleName() + "-deploy/src/main/resources/";
            String newResourcePath = new URL(classPathURL, relativePath).getFile();

            //覆盖系统变量,这样才能找到新的配置文件
            System.setProperty("props.path", newResourcePath);

            //使用filter值覆盖env里面的值;
            cpProperties(
                newResourcePath + File.separator + "filters" + File.separator + springTestConfig.getPropertyName(),
                newResourcePath + File.separator + ConfigsUtils.ENV_PROPERTIES_FILE_NAME);

            //递归resourc文件
            relativePath = "../../src/main/resources/";
            newResourcePath = new URL(classPathURL, relativePath).getFile();
            FileUtils.copyDirectiory(newResourcePath, classPath);
        } catch (MalformedURLException e) {
            LOGGER.error("file init fail", e);
        }

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

    static class DefaultSpringTestConfig implements SpringTestConfig {

        @Override
        public String getPropertyName() {
            return "dev.properties";
        }

        /**获得应用简称*/
        public String getAppSimpleName() {
            return getAppSimpleName(getClass().getClassLoader().getResource("").getFile());
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
