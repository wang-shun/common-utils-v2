package com.youzan.sz.common.base.simple;

import com.youzan.sz.DistributedCallTools.DistributeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Created by zhanguo on 16/8/18.
 */
public abstract class BaseServiceImpl implements DistributeAttribute {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
}
