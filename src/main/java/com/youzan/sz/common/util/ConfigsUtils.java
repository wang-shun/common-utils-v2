package com.youzan.sz.common.util;

import java.io.File;

/**
 *
 * Created by zhanguo on 16/7/19.
 */
public class ConfigsUtils {
    /**配置文件夹绝对路径*/
    public final static String CONFIG_DIR_PATH           = System.getProperty("props.path","");
    /**与环境相关文件名*/
    public final static String ENV_PROPERTIES_FILE_NAME  = "config-env.properties";
    /**业务配置文件名*/
    public final static String APP_PROPERTIES_FILE_NAME  = "config-app.properties";
    /**基础配置文件名*/
    public final static String BASE_PROPERTIES_FILE_NAME = "config-base.properties";

    public final static String CONTAINER_PROPERTIES_FILE_NAME  = "container.properties";

    public final static String APP_NAME                  = "app.name";

    public final static String ZK_ADDRESS                = "zk.address";

    public final static String DUBBO_PORT                = "dubbo.port";

    public final static String CONFIG_APP_FILE_PATH      = CONFIG_DIR_PATH + File.separator + APP_PROPERTIES_FILE_NAME;
    public final static String CONFIG_BASE_FILE_PATH     = CONFIG_DIR_PATH + File.separator + BASE_PROPERTIES_FILE_NAME;
    public final static String CONFIG_ENV_FILE_PATH     = CONFIG_DIR_PATH + File.separator + ENV_PROPERTIES_FILE_NAME;
    public final static String CONFIG_CONTAINER_FILE_PATH     = CONFIG_DIR_PATH + File.separator + CONTAINER_PROPERTIES_FILE_NAME;

}
