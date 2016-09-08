package com.youzan.sz.common.base.simple;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.DistributedCallTools.DistributeAttribute;
import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.base.BaseDao;
import com.youzan.sz.common.enums.LogBizType;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.interfaces.DevModeEnable;
import com.youzan.sz.common.interfaces.ToolKits;
import com.youzan.sz.common.model.base.BasePO;
import com.youzan.sz.common.model.base.BaseProcessor;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/16.
 */
public abstract class SimpleProcessor<T extends BasePO<ID>, ID extends Serializable> extends BaseProcessor
                                     implements ToolKits {

    public int save(T t) {
        if (t.getId() == null) {
            return insert(t);
        } else {
            return update(t);
        }
    }

    public T findOne(T queryPO) {
        return getDao().findOne(queryPO);
    }

    public List<T> findList(T queryPO) {
        return getDao().findList(queryPO);
    }

    protected abstract BaseDao<T> getDao();

    public int insert(T t) {
        return getDao().insert(t);
    }

    public int update(T t) {
        return getDao().update(t);
    }
}
