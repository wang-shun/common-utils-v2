package com.youzan.sz.common.base.simple;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributeAttribute;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.base.BaseDao;
import com.youzan.sz.common.enums.LogBizType;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.model.base.BasePO;
import com.youzan.sz.common.model.base.BaseProcessor;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/16.
 */
public abstract class SimpleProcessor<T extends BasePO<ID>, ID extends Serializable> extends BaseProcessor
                                     implements DistributeAttribute {

    public int save(T t) {
        if (t.getId() == null) {
            return getDao().insert(t);
        } else {
            return getDao().update(t);
        }
    }

    protected abstract BaseDao<T> getDao();
}
