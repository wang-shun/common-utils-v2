package com.youzan.sz.common.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/9/13.
 */
public interface LogTools {
    default void shopLog(String tag, IShop iShop) {
        if (getLogger().isInfoEnabled())
            getLogger().info("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }

    default void debugShopLog(String tag, IShop iShop) {
        if (getLogger().isDebugEnabled())
            getLogger().info("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }

    default void errorShopLog(String tag, IShop iShop) {
        if (getLogger().isErrorEnabled())
            getLogger().error("bid:{},shopId:{}," + tag, iShop.getBid(), iShop.getShopId());
    }

    default Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }

}
