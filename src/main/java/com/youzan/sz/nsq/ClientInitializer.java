package com.youzan.sz.nsq;

import com.youzan.sz.common.util.ConfigsUtils;
import com.youzan.sz.common.util.PropertiesUtils;

/**
 * <p>对响应时间(response time)要求较高的lookup地址</p>
 * 需要配置地址:<code>nsq.rt.host=nsq-dev.s.qima-inc.com:4161</code>
 * @see <a href="http://doc.qima-inc.com/display/engineer/NSQ">nsq地址列表</a>
 * */
public interface ClientInitializer {
    /**
     * 默认的nsq地址
     * */
    default String getLookupDefault() {
        return PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.host");
    }

    default String getLookupRt() {
        return PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH, "nsq.rt.host");
    }

    void build();

}
