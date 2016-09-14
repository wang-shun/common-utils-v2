package com.youzan.sz.common.base.simple;

import com.youzan.sz.DistributedCallTools.DistributeAttribute;
import com.youzan.sz.common.interfaces.DevModeEnable;
import com.youzan.sz.common.interfaces.ToolKits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/8/18.
 */
public abstract class BaseServiceImpl implements ToolKits {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Logger getLogger() {
        return logger;
    }
}
