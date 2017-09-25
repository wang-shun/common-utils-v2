package com.youzan.sz.sign;

import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.service.AuthService;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.SpringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class SignTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignTools.class);

    private static final Object INIT_MUTEX = new Object();

    private static SignTools instance = null;

    private static AuthService authService;


    private SignTools() {
    }


    /**
     * 考虑到性能原因,这里不做可见性检查,因为只有刚开始启动时,可能出现可见性问题,稍微晚一会儿获取到authService也可以;
     */
    public static SignTools getInstance() {
        if (authService == null) {
            init();
        }
        return instance;
    }


    public static void init() {
        synchronized (INIT_MUTEX) {
            if (authService != null) {
                return;
            }
            authService = SpringUtils.getBean(AuthService.class);
            if (authService == null) {
                LOGGER.warn("authService 还未初始化,无法使用");
                return;
            }
            instance = new SignTools();
        }
    }
    
    public static boolean sign(String paramJson){
        Map<String, String> params = JsonUtils.json2Bean(paramJson, Map.class);
        String sign = DistributedContextTools.getSign();
        LOGGER.info("call authService.sign param:{} sign:{}", params, sign);
        return authService.sign(params, sign);
    }

}
