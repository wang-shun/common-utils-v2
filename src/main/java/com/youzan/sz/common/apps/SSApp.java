package com.youzan.sz.common.apps;

/**
 *
 * Created by zhanguo on 16/8/18.
 *
 */
public interface SSApp extends IAPP {
    default APP_ENUM getAPP() {
        return APP_ENUM.SS;
    }
}
