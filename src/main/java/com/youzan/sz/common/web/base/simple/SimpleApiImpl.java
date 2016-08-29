package com.youzan.sz.common.web.base.simple;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributeAttribute;
import com.youzan.sz.common.enums.LogBizType;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.interfaces.DevModeEnable;
import com.youzan.sz.common.interfaces.ToolKits;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.oa.shop.ShopService;
import org.springframework.stereotype.Service;

import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.apps.IAPP;
import com.youzan.sz.common.enums.AppEnum;
import com.youzan.sz.common.model.base.BaseApiImpl;

/**
 *
 * Created by zhanguo on 16/8/16.
 */
@Service
public abstract class SimpleApiImpl extends BaseApiImpl implements ToolKits {
    private final static Map<AppEnum, List<IAPP>> SERVICE_MAP  = new HashMap<>();
    private final static ExecutorService          LOG_EXECUTOR = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void registerService() {
        final Field[] fields = this.getClass().getFields();
        if (fields != null) {
            for (Field field : fields) {
                final Class<?> filedClazz = field.getDeclaringClass();
                if (filedClazz.isAssignableFrom(IAPP.class)) {
                    try {
                        IAPP fieldInstance = null;
                        final boolean accessible = field.isAccessible();
                        if (!accessible) {
                            field.setAccessible(true);
                        }
                        fieldInstance = (IAPP) field.get(this);
                        if (fieldInstance == null) {
                            logger.warn("field({}) is null", field.getName());
                        } else {
                            List<IAPP> serviceList = SERVICE_MAP.get(fieldInstance.getAPP());
                            if (serviceList == null) {
                                serviceList = new ArrayList<>();
                            }
                            serviceList.add(fieldInstance);
                            SERVICE_MAP.put(fieldInstance.getAPP(), serviceList);

                        }
                    } catch (IllegalAccessException e) {
                        logger.error("init service error", e);
                    }
                }
            }
        }
    }

    protected <T> T getService(Class<T> clazz) {
        final Integer aId = DistributedContextTools.getAId();
        final AppEnum appByAid = AppEnum.getAppByAid(aId);
        if (appByAid == null) {//登陆时候必须指定一个应用类型
            logger.error("未获取到appId,数据异常");
        }
        final List<IAPP> appServiceList = SERVICE_MAP.get(appByAid);
        if (appServiceList == null) {
            logger.info("未找到应用({})的service,使用通用service", appByAid.getName());
        }
        for (IAPP service : appServiceList) {
            if (service.getClass().isAssignableFrom(clazz)) {
                return (T) service;
            }
        }
        logger.error("未找到支持此应用({})的服务({})", appByAid.getName(), clazz.getCanonicalName());
        return null;
    }

    protected void asyncLog(LogBizType logBizType) {
        final Long adminId = getAdminId();
        final Long bId = getBid();
        final String deviceId = getDeviceId();
        LOG_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                logger.debug("保存{}日志", logBizType.getDesc());
            }
        });
    }
}
