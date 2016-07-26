package com.youzan.sz.dubbo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.youzan.sz.common.util.SpringUtils;
import com.youzan.sz.monitor.HeathCheck;

/**
 *
 * Created by zhanguo on 16/7/20.
 */
public class DubboUtils {
    private final static Logger LOGGER      = LoggerFactory.getLogger(com.youzan.sz.dubbo.DubboUtils.class);
    static ApplicationConfig    application = null;
    static RegistryConfig       registry    = null;
    static ProtocolConfig       protocol    = null;

    // 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口

    /**发布服务*/
    public static <T> void deployService(T t) {
        // 服务实现
        // 服务提供者暴露服务配置
        ServiceConfig<T> service = new ServiceConfig<T>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
        application = SpringUtils.getBean(ApplicationConfig.class);
        registry = SpringUtils.getBean(RegistryConfig.class);
        protocol = SpringUtils.getBean(ProtocolConfig.class);
        service.setApplication(application);
        service.setRegistry(registry); // 多个注册中心可以用setRegistries()
        service.setProtocol(protocol); // 多个协议可以用setProtocols()
        service.setInterface(HeathCheck.class);
        service.setRef(t);
        service.setVersion("1.0.0");
        // 暴露及注册服务
        service.export();
    }

}
