package com.youzan.sz.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ProtocolConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import com.youzan.sz.common.util.SpringUtils;
import com.youzan.sz.monitor.HeathCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.youzan.sz.common.util.SpringUtils.getBean;

/**
 *
 * Created by zhanguo on 16/7/20.
 */
public class DubboUtils {
    private final static Logger LOGGER      = LoggerFactory.getLogger(com.youzan.sz.dubbo.DubboUtils.class);
    static ApplicationConfig    application = null;
    static List<RegistryConfig> registrys   = new ArrayList<>();
    static List<ProtocolConfig> protocols   = new ArrayList<>();

    // 注意：ServiceConfig为重对象，内部封装了与注册中心的连接，以及开启服务端口

    /**发布服务*/
    public static <T> void deployService(T t) {
        // 服务实现
        // 服务提供者暴露服务配置
        ServiceConfig<T> service = new ServiceConfig<T>(); // 此实例很重，封装了与注册中心的连接，请自行缓存，否则可能造成内存和连接泄漏
        application = getBean(ApplicationConfig.class);
        // registry = SpringUtils.getBean(RegistryConfig.class);
        try {
            RegistryConfig zkRegistry = (RegistryConfig) getBean("zookeeper");
            if (zkRegistry != null) {
                LOGGER.info("zk Register config add {}", zkRegistry);
                registrys.add(zkRegistry);
            }
            RegistryConfig hauntRegistry = (RegistryConfig) getBean("haunt");
            if (hauntRegistry != null) {
                LOGGER.info("etcd Register config add {}", hauntRegistry);
                registrys.add(hauntRegistry);
            }
        } catch (Exception e) {
            LOGGER.info("multi register center error {}", e);
        }

        service.setApplication(application);

        if (registrys.size() == 0) {
            registrys.add(getBean(RegistryConfig.class));
        }
        service.setRegistries(registrys); // 多个注册中心可以用setRegistries()
        try {
            ProtocolConfig dubboProtocol = (ProtocolConfig) SpringUtils.getBean("dubbo");
            if (dubboProtocol != null) {
                LOGGER.info("dubboProtocol   add {}", dubboProtocol);
                protocols.add(dubboProtocol);
            }
            ProtocolConfig novaProtocol = (ProtocolConfig) SpringUtils.getBean("nova");
            if (novaProtocol != null) {
                LOGGER.info("novaProtocol   add {}", novaProtocol);
                protocols.add(novaProtocol);
            }
        } catch (Exception e) {
            LOGGER.info("multi protocol error {}", e);
        }
        if (protocols.size() == 0) {
            protocols.add(SpringUtils.getBean(ProtocolConfig.class));
        }
        service.setProtocols(protocols); // 多个协议可以用setProtocols()

        service.setInterface(HeathCheck.class);
        service.setRef(t);
        service.setVersion("1.0.0");
        // 暴露及注册服务
        service.export();
    }

}
