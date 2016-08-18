package com.youzan.sz.common.apps;

import com.youzan.sz.common.enums.AppEnum;

/**
 *
 * Created by zhanguo on 16/8/18.
 * 免费收银版本
 */
public interface FCApp extends SSApp {
    @Override
    default AppEnum getAPP() {
        return AppEnum.FC;
    }
}
