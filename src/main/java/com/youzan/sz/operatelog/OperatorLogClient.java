package com.youzan.sz.operatelog;

import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.enums.BusinessTypeV2;
import com.youzan.sz.common.enums.OperateTypeV2;
import com.youzan.sz.common.enums.RoleEnum;
import com.youzan.sz.common.model.operatelog.OperateLogV2;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.service.OperateMinService;
import com.youzan.sz.common.util.SpringUtils;
import com.youzan.sz.session.SessionTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jinxiaofei on 16/10/27.
 */
public final class OperatorLogClient {
    private static OperatorLogClient instance;
    private static final Logger LOGGER= LoggerFactory.getLogger(OperatorLogClient.class);
    private static OperateMinService operateMinService;

    public static OperatorLogClient getInstance() {
        if(instance!=null){
            return instance;
        }
        if (instance != null) {
            synchronized (instance){
                final OperateMinService operateService= SpringUtils.getBean(OperateMinService.class);
                if (operateService == null) {
                    LOGGER.warn(
                            "the operateMinService not ready, check the service config add operateMinService to rpc service");
                    throw ResponseCode.ERROR_INNER.getBusinessException();
                }
                instance = new OperatorLogClient(operateService);
            }
        }
        return instance;
    }

    private  OperatorLogClient(OperateMinService operateService) {
        operateMinService=operateService;
    }

    public void write(BusinessTypeV2 businessType, OperateTypeV2 operateTypeV2,String businessValue){
        final OperateLogV2 operateLogV2 = new OperateLogV2();
        operateLogV2.setOperateIp(DistributedContextTools.getRequestIp());
        operateLogV2.setShopId(DistributedContextTools.getShopId());
        operateLogV2.setBid(DistributedContextTools.getBid());
        operateLogV2.setBusinessTypeV2(businessType);
        operateLogV2.setOperateTypeV2(operateTypeV2);
        operateLogV2.setStaffId(Long.parseLong(SessionTools.getInstance().get(SessionTools.STAFF_ID)));
        operateLogV2.setStaffName(SessionTools.getInstance().get(SessionTools.STAFF_NAME));
        operateLogV2.setStaffRole(RoleEnum.valueOf(Integer.valueOf(SessionTools.getInstance().get(SessionTools.ROLE))).getName());
        operateLogV2.setBusinessValue(businessValue);
        LOGGER.debug("开始保存V2操作日志:{}",operateLogV2);
        operateMinService.write(operateLogV2);
    }
    
    
    /**
     * 退款的操作日志
   
     */
    public void writeRefund(String orderNo,String money){
        final OperateLogV2 operateLogV2 = new OperateLogV2();
        operateLogV2.setOperateIp(DistributedContextTools.getRequestIp());
        operateLogV2.setShopId(DistributedContextTools.getShopId());
        operateLogV2.setBid(DistributedContextTools.getBid());
        operateLogV2.setBusinessTypeV2(BusinessTypeV2.REFUND);
        operateLogV2.setOperateTypeV2(OperateTypeV2.ADD);
        operateLogV2.setStaffId(Long.parseLong(SessionTools.getInstance().get(SessionTools.STAFF_ID)));
        operateLogV2.setStaffName(SessionTools.getInstance().get(SessionTools.STAFF_NAME));
        operateLogV2.setStaffRole(RoleEnum.valueOf(Integer.valueOf(SessionTools.getInstance().get(SessionTools.ROLE))).getName());
        String businessValue=BusinessTypeV2.REFUND.getFormat().replace("${money}",money).replace("${No}",orderNo);
        operateLogV2.setBusinessValue(businessValue);
        LOGGER.debug("开始保存V2操作日志:{}",operateLogV2);
        operateMinService.write(operateLogV2);
    }

    public void write(OperateLogV2 operateLogV2){
        LOGGER.debug("开始保存V2操作日志:{}",operateLogV2);
        operateMinService.write(operateLogV2);
    }
}
