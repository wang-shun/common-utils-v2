package com.youzan.sz.common.base.simple;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;

import com.youzan.sz.common.base.BaseDao;
import com.youzan.sz.common.interfaces.ToolKits;
import com.youzan.sz.common.model.base.BasePO;
import com.youzan.sz.common.model.base.BaseProcessor;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/8/16.
 */
public abstract class SimpleServiceImpl implements ToolKits {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Logger getLogger() {
        return logger;
    }
}
