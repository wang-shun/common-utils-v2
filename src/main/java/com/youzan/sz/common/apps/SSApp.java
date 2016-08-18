package com.youzan.sz.common.apps;

import com.youzan.sz.common.enums.AppEnum;

/**
 *
 * Created by zhanguo on 16/8/18.
 *
 */
public interface SSApp extends IAPP {
    default AppEnum getAPP() {
        return AppEnum.SS;
    }
}
