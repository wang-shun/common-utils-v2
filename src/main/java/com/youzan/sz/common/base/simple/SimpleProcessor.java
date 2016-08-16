package com.youzan.sz.common.base.simple;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.youzan.platform.util.lang.StringUtil;
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
public abstract class SimpleProcessor<T extends BasePO<ID>, ID extends Serializable> extends BaseProcessor {
    private final static ExecutorService LOG_EXECUTOR = Executors.newFixedThreadPool(2);

    public String getDeviceId() {
        final String deviceId = DistributedContextTools.getDeviceId();
        if (StringUtil.isEmpty(deviceId)) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少deviceId");
        }
        return deviceId;
    }

    public Long getAdminId() {
        final Long adminId = DistributedContextTools.getAdminId();
        if (adminId == null || adminId == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少adminId");
        }
        return adminId;
    }

    public Long getBId() {
        final Long bid = DistributedContextTools.getKdtId();
        if (bid == null || bid == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少bid");
        }
        return bid;
    }

    protected void asyncLog(LogBizType logBizType) {
        final Long adminId = getAdminId();
        final Long bId = getBId();
        final String deviceId = getDeviceId();
        LOG_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                logger.debug("保存{}日志", logBizType.getDesc());
            }
        });
    }

    protected int save(T t) {
        if (t.getId() == null) {
            return getDao().insert(t);
        } else {
            return getDao().update(t);
        }
    }

    protected abstract BaseDao<T> getDao();
}
