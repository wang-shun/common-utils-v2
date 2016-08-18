package com.youzan.sz.common.base.simple;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

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
public abstract class SimpleApiImpl extends BaseApiImpl {
    private final static Map<AppEnum, List<IAPP>> SERVICE_MAP = new HashMap<>();

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
                            if (SERVICE_MAP.containsKey(fieldInstance.getAPP())) {
                                SERVICE_MAP.get(fieldInstance.getAPP()).add(fieldInstance);
                            }
                            //                            SERVICE_MAP.put(fieldInstance.getAPP(), fieldInstance);

                        }
                    } catch (IllegalAccessException e) {
                        logger.error("init service error", e);
                    }
                }
            }
        }

    }

    protected <T> T getService(Class<T> clazz) {
        final String aId = DistributedContextTools.getAId();
        final AppEnum appByAid = AppEnum.getAppByAid(aId);
        if (appByAid == null) {//登陆时候必须指定一个应用类型
            logger.error("未获取到appId,数据异常");
        }
        final List<IAPP> iapp = SERVICE_MAP.get(appByAid);
        if (iapp == null) {
            logger.info("未找到应用({})的service,使用通用service", appByAid.getDesc());
        }
        return null;
    }
}
