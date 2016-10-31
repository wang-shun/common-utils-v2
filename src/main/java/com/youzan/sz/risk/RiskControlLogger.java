package com.youzan.sz.risk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by jinxiaofei on 16/10/31.
 */
public class RiskControlLogger {
    private static final Logger LOGGER= LoggerFactory.getLogger(RiskControlLogger.class);

    public static final void printLogger(String module, Map params){
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("RISK CONTROL ,MODULE:{},PARAMS:{}",module,params);
        }
    }
    public static final void printLogger(String module, String params){
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("RISK CONTROL ,MODULE:{},PARAMS:{}",module,params);
        }
    }
}
