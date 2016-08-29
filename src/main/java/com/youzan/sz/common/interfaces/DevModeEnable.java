package com.youzan.sz.common.interfaces;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

/**
 *
 * Created by zhanguo on 16/8/28.
 */
public interface DevModeEnable {
    String DEV_MODEL = "dev.mode.enable";

    /**是否开启了开发模式.
     * 可以用来去掉一些限制
     * */
    default boolean isDevModel() {
        final String property = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, DEV_MODEL, "0");
        return "1".equals(property);
    }
}
