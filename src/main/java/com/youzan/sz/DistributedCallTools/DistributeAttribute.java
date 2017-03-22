package com.youzan.sz.DistributedCallTools;

import com.youzan.platform.util.lang.StringUtil;
import com.youzan.sz.common.AidBizEnum;
import com.youzan.sz.common.exceptions.BizException;
import com.youzan.sz.common.interfaces.IShop;
import com.youzan.sz.common.model.base.BaseOperator;
import com.youzan.sz.common.model.enums.DeviceType;
import com.youzan.sz.common.response.enums.ResponseCode;


/**
 * Created by zhanguo on 16/8/22.
 */
public interface DistributeAttribute extends IShop {
    
    default Long getAdminId() {
        final Long adminId = DistributedContextTools.getAdminId();
        if (adminId == null || adminId == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少adminId");
        }
        return adminId;
    }
    
    /**
     * app版本信息
     */
    default String getAppVersion() {
        final String appVersion = DistributedContextTools.getAppVersion();
        if (StringUtil.isEmpty(appVersion)) {
            // 这里先不抛出异常信息，只是简单的打印日志信息
            //throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少opAdminName");
        }
        return appVersion;
    }
    
    /**
     * 获取基础操作者
     */
    default BaseOperator getBaseOperator() {
        String opName = DistributedContextTools.getOpAdminName();
        Long opId = DistributedContextTools.getOpAdminId();
        if (opId == null) {
            opId = 0L;
        }
        if (opName == null)
            opName = "未指定操作者,查看是未赋值或切换线程";
        return new BaseOperator(opId, opName);
    }
    
    default String getBiz() {
        String biz = null;
        if (AidBizEnum.valueOf(getAId()) != null) {
            biz = AidBizEnum.valueOf(getAId()).getName();
        }
        return biz;
    }
    
    /**
     * 在指定店铺后就会拥有
     */
    default Integer getAId() {
        final Integer aid = DistributedContextTools.getAId();
        if (aid == null || aid == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少aid");
        }
        return aid;
    }
    
    default String getDeviceId() {
        final String deviceId = DistributedContextTools.getDeviceId();
        //不是web登录,必须获取deviceId
        if (!String.valueOf(DeviceType.WEB.getValue()).equals(getDeviceType()) && StringUtil.isEmpty(deviceId)) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少deviceId");
        }
        return deviceId;
    }
    
    /**
     * 在指定店铺后就会拥有
     */
    default String getDeviceType() {
        final String deviceType = DistributedContextTools.getDeviceType();
        if (deviceType == null) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少deviceType,请选择店铺");
        }
        return deviceType;
    }
    
    default Long getKdtId() {
        final Long kdtId = DistributedContextTools.getKdtId();
        if (kdtId == null) {//如果拿不到,则使用bid
            final Long bid = DistributedContextTools.getBid();
            if (bid == null || bid == 0) {
                throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少kdtId");
            }else {
                return bid;
            }
        }
        return kdtId;
    }
    
    /**
     * 管理cp必传字段
     */
    default Long getOpId() {
        final Long opAdminId = DistributedContextTools.getOpAdminId();
        if (opAdminId == null || opAdminId == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少opAdminId");
        }
        return opAdminId;
    }
    
    /**
     * 设置操作者
     */
    default DistributeAttribute setOpId(Long t) {
        DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.OpAdminId.class, t);
        return this;
    }
    
    /**
     * 管理cp拥有
     */
    default String getOpName() {
        final String opAdminName = DistributedContextTools.getOpAdminName();
        if (opAdminName == null) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文缺少opAdminName");
        }
        return opAdminName;
    }
    
    /**
     * 设置操作者
     * 为了减少传递参数
     */
    default DistributeAttribute setOpName(String opName) {
        DistributedContextTools.setAttr(DistributedContextTools.DistributedParamManager.OpAdminName.class, opName);
        return this;
    }
    
    /**
     * 在指定店铺后就会拥有
     */
    @Override
    default Long getShopId() {
        final Long shopId = DistributedContextTools.getShopId();
        //这里不考虑shopId=0.因为新版店铺都为0|| shopId == 0
        //        if (shopId == null ) {
        //            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少shopId");
        //        }
        return shopId;
    }
    
    @Override
    default Long getBid() {
        final Long bid = DistributedContextTools.getBId();
        if (bid == null || bid == 0) {
            throw new BizException(ResponseCode.PARAMETER_ERROR, "上下文中缺少bid");
        }
        return bid;
    }
}
