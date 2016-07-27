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
public abstract class BaseSpringTest extends BaseTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(BaseSpringTest.class);

    @BeforeClass
    public static void initClass() {
        try {

            URL classPathURL = BaseSpringTest.class.getClassLoader().getResource("");
            String classPath = classPathURL.getFile();
            LOGGER.info("current classPath path:{}", classPath);
            String relativePath = "../../../" + getAppSimpleName() + "-deploy/src/main/resources/";
            String newResourcePath = new URL(classPathURL, relativePath).getFile();
            //覆盖系统变量,这样才能找到新的配置文件
            System.setProperty("props.path", newResourcePath);
            //使用filter值覆盖env里面的值;
            cpProperties(newResourcePath + File.separator + "filters" + File.separator + "dev.properties",
                newResourcePath + File.separator + ConfigsUtils.ENV_PROPERTIES_FILE_NAME);

            //递归resourc文件
            relativePath = "../../src/main/resources/";
            newResourcePath = new URL(classPathURL, relativePath).getFile();
            //配置文件放置到classpath中
            //        cpXmlFileToClsPath(newResourcePath, classPath);
            //dal直接完全复制文件夹
            LOGGER.info("开始复制配置XML,from {} To {}", relativePath, classPath);

            FileUtils.copyDirectiory(newResourcePath, classPath);
        } catch (MalformedURLException e) {
            LOGGER.error("file init fail", e);
        }

    }

    public static void cpProperties(String srcFilePath, String targetFilePath) {
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

    /**获得应用简称*/
    public static String getAppSimpleName() {
        return getAppSimpleName(BaseSpringTest.class.getClassLoader().getResource("").getFile());
    }

    public static String getAppSimpleName(String filePath) {
        String[] pathArray = filePath.split(File.separator);
        for (int i = pathArray.length - 1; i > 0; i--) {
            if (pathArray[i].startsWith(pathArray[i - 1] + "-")) {//项目模块以"-"分割
                return pathArray[i - 1];
            }
        }
        LOGGER.error("未能识别项目名字,请覆盖获取项目名字方法");
        return null;
    }

    //    public static void main(String[] args) {
    //        String file = "/Users/vincentbu/IdeaProjects/goods/goods-core/target/test-classes";
    //        System.out.println(getAppSimpleName(file));
    //    }
}
