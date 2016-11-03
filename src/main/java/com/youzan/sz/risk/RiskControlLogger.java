package com.youzan.sz.risk;

import com.youzan.sz.common.enums.AppEnum;
import com.youzan.sz.common.model.enums.RiskEventEnum;
import com.youzan.sz.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jinxiaofei on 16/10/31.
 */
public class RiskControlLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger(RiskControlLogger.class);
    private static final String MODULE_NAME = "freecash";
    public static final String EVENT_TYPE = "event_type";
    public static final String EVENT_TIMESTAMP = "event_timestamp";
    public static final String DEVICE_ID = "device_id";
    public static final String PHONE = "phone";
    public static final String MONEY = "money";//单位分
    public static final String AUTH_TYPE = "auth_type";
    public static final String PAY_TYPE = "pay_type";
    public static final String OPERATOR = "operator";
    public static final String IP = "ip";
    public static final String SHOP_ID = "shop_id";
    public static final String DATA = "data";
    public static final String CARD = "card";
    public static final String TABLE_CARD_CODE = "table_card_id";
    public static final String OPEN_ID = "open_id";





    public static final void printLogger(RiskEventEnum riskEventEnum, Map<String,Object> params){
        if(riskEventEnum==null){
            LOGGER.info("事件类型为空,不在打印日志,param:{}",params);
            return;
        }
        params.put(EVENT_TYPE,riskEventEnum.getValue());
        params.put(EVENT_TIMESTAMP,new Date());
        if(LOGGER.isInfoEnabled()){
            LOGGER.info("RISK CONTROL,MODULE:{},EVENT_NAME:{},EVENT_TYPE:{},PARAMS:{}",MODULE_NAME,riskEventEnum.getCname(),riskEventEnum.getValue(), JsonUtils.bean2Json(params));
        }
    }
    public static final void printLogger(RiskEventEnum riskEventEnum, String params){

        if(LOGGER.isInfoEnabled()){
            LOGGER.info("RISK CONTROL,MODULE:{},EVENT_NAME:{},EVENT_TYPE:{},PARAMS:{}",MODULE_NAME,riskEventEnum.getCname(),riskEventEnum.getValue(), params);
        }
    }

    public static void main(String[] args) {
        Map<String,Object> params=new HashMap<>();
        params.put("account","15814936408");
        params.put("ip","127.0.0.1");
        RiskControlLogger.printLogger(RiskEventEnum.LOGIN,params);
        params.put("card","6204226079190556444");
        RiskControlLogger.printLogger(RiskEventEnum.WITHDRAW,params);
    }
}
