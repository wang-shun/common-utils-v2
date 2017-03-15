package com.youzan.sz.common.interfaces;

import com.youzan.sz.common.util.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zhanguo on 16/9/13.
 */
public interface LogTools {


    default void debugShopLog(String tag, IShop iShop) {
        if (getLogger().isDebugEnabled())
            getLogger().info("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }
    
    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

    default void errorShopLog(String tag, IShop iShop) {
        if (getLogger().isErrorEnabled())
            getLogger().error("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }
    
    default void infoLog(String message, Object... objs) {
        if (getLogger().isInfoEnabled()) {
            Object[] params = new Object[objs.length];
            for (int i = 0; i < objs.length; i++) {
                params[i] = JsonUtils.toJson(objs[i]);
            }
            getLogger().info(message, params);
        }
    }
    
    default void warnLog(String message, Object... objs) {
        if (getLogger().isErrorEnabled()) {
            Object[] params = new Object[objs.length];
            for (int i = 0; i < objs.length; i++) {
                params[i] = JsonUtils.toJson(objs[i]);
            }
            getLogger().error(message, params);
        }
    }
    
    default void errorLog(String message, Throwable e) {
        if (getLogger().isErrorEnabled()) {
            getLogger().error(message, e);
        }
    }
    
    default void errorLog(String message, Object... objs) {
        if (getLogger().isErrorEnabled()) {
            Object[] params = new Object[objs.length];
            for (int i = 0; i < objs.length; i++) {
                params[i] = JsonUtils.toJson(objs[i]);
            }
            getLogger().error(message, params);
        }
    }
    
    default void shopLog(String tag, IShop iShop) {
        if (getLogger().isInfoEnabled())
            getLogger().info("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }

}
