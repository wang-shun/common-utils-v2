package com.youzan.sz.common.apps;

/**
 *
 * Created by zhanguo on 16/8/18.
 * 免费收银版本
 */
public interface FCApp extends SSApp {
    @Override
    default APP_ENUM getAPP() {
        return APP_ENUM.FC;
    }
}
