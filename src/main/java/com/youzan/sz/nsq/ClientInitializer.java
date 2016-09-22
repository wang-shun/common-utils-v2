package com.youzan.sz.nsq;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public interface ClientInitializer {
    /**
     * 默认的nsq地址
     * */
    default String getLookupDefault() {
        return PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host");
    }

    /**
     * 对响应时间(response time)要求较高的lookup地址
     * */
    default String getLookupRt() {
        return PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.rt.host");
    }

}
