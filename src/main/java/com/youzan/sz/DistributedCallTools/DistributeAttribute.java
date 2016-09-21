package com.youzan.sz.DistributedCallTools;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.interfaces.IShop;
import com.youzan.sz.common.model.enums.DeviceType;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.session.SessionTools;

/**
 *
 * Created by zhanguo on 16/8/22.
 */
public interface DistributeAttribute extends IShop {

    default String getDeviceId() {
        final String deviceId = DistributedContextTools.getDeviceId();
        //不是web登录,必须获取deviceId
        if (!String.valueOf(DeviceType.WEB.getValue()).equals(getDeviceType()) && StringUtil.isEmpty(deviceId)) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少deviceId");
        }
        return deviceId;
    }

    default Long getAdminId() {
        final Long adminId = DistributedContextTools.getAdminId();
        if (adminId == null || adminId == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少adminId");
        }
        return adminId;
    }

    @Override
    default Long getBid() {
        final Long bid = DistributedContextTools.getBId();
        if (bid == null || bid == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少bid");
        }
        return bid;
    }

    /**
     * 在指定店铺后就会拥有
     * */
    @Override
    default Long getShopId() {
        final Long shopId = DistributedContextTools.getShopId();
        if (shopId == null || shopId == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少shopId");
        }
        return shopId;
    }

    /**
     * 在指定店铺后就会拥有
     * */
    default Integer getAId() {
        final Integer aid = DistributedContextTools.getAId();
        if (aid == null || aid == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少aid");
        }
        return aid;
    }

    /**
     * 在指定店铺后就会拥有
     * */
    default String getDeviceType() {
        final String deviceType = DistributedContextTools.getDeviceType();
        if (deviceType == null) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少deviceType");
        }
        return deviceType;
    }

}
