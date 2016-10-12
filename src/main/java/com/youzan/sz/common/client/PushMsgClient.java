package com.youzan.sz.common.client;

import java.util.Map;

import com.youzan.sz.DistributedCallTools.DistributedContextTools;
import com.youzan.sz.common.model.Page;
import com.youzan.sz.common.model.base.BaseStaffDTO;
import com.youzan.sz.common.model.enums.MsgTypeEnum;
import com.youzan.sz.common.push.PushMsgDTO;
import com.youzan.sz.common.push.SendType;
import com.youzan.sz.common.push.msg.MsgDTO;
import com.youzan.sz.common.push.msg.MsgPageDTO;
import com.youzan.sz.common.response.BaseResponse;
import com.youzan.sz.common.service.PushDelegateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.SpringUtils;

import javax.validation.constraints.NotNull;

/**
 * Created by zefa on 16/7/5.
 */
public final class PushMsgClient {
    private static PushMsgClient       instance            = null;
    private static final Object        mutex               = new Object();
    private static final Logger        logger              = LoggerFactory.getLogger(PushMsgClient.class);
    private static PushDelegateService pushDelegateService = null;

    /**
     * 其实这里多初始化两次没关系
     * **/
    public static PushMsgClient getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (mutex) {
            if (instance == null) {
                final PushDelegateService delegateService = SpringUtils.getBean(PushDelegateService.class);
                if (delegateService == null) {
                    logger.error(
                        "the push service not ready, check the service config add pushDelegateService to rpc service");
                    throw ResponseCode.PUSH_SERVICE_NOT_EXIST.getBusinessException();
                }
                instance = new PushMsgClient(delegateService);
            }
        }
        return instance;
    }

    private PushMsgClient(PushDelegateService delegateService) {
        pushDelegateService = delegateService;
    }

    /**
     * @param staffDTO  为了支持以后多店铺,发送时需要指定bid,shopId,adminId
     *@param msgTypeEnum 消息类型,前端采用不同的处理逻辑,某些消息不会存储到消息列表
     *@param content 消息内容
     * */
    public void pushAdminIdMsg(BaseStaffDTO staffDTO, MsgTypeEnum msgTypeEnum, @NotNull String title,
                               @NotNull String content, Map<String, String> params) {
        final PushMsgDTO pushMsgDTO = new PushMsgDTO();
        pushMsgDTO.setMsgType(msgTypeEnum.getValue());
        pushMsgDTO.setContent(content);
        pushMsgDTO.setTitle(title);
        pushMsgDTO.setSendType(SendType.ADMIN_ID.getValue());
        pushMsgDTO.addStaffRecv(staffDTO);
        pushMsgDTO.setParams(params);
        pushMsg(pushMsgDTO);
    }

    /**
     * 按照bid+shopId+roleId推送
     * */
    public void pushRoleMsg(BaseStaffDTO staffDTO, MsgTypeEnum msgTypeEnum, @NotNull String content,
                            @NotNull String title, Map<String, String> params) {
        final PushMsgDTO pushMsgDTO = new PushMsgDTO();
        pushMsgDTO.setMsgType(msgTypeEnum.getValue());
        pushMsgDTO.setContent(content);
        pushMsgDTO.setSendType(SendType.SHOP_ROLE.getValue());
        pushMsgDTO.addStaffRecv(staffDTO);
        pushMsgDTO.setParams(params);
        pushMsg(pushMsgDTO);
    }

    public void pushMsg(PushMsgDTO pushMsgDTO) {
        if (logger.isInfoEnabled()) {
            logger.info("start push msg:{}", JsonUtils.toJson(pushMsgDTO));
        }
        final BaseResponse<PushMsgDTO> pushMsgDTOBaseResponse = pushDelegateService.pushMsg(pushMsgDTO);
        if (logger.isInfoEnabled()) {
            logger.info("end push msg:{} ,result:{}", JsonUtils.toJson(pushMsgDTO), pushMsgDTOBaseResponse.isSucc());
        }
    }

    public Page<MsgDTO> getMsgList(MsgPageDTO msgPageDTO) {
        if (msgPageDTO.getAdminId() == null) {
            final Long adminId = DistributedContextTools.getAdminId();
            msgPageDTO.setAdminId(adminId);
        }

        if (msgPageDTO.getBid() == null) {
            final Long bId = DistributedContextTools.getBId();
            msgPageDTO.setBid(bId);
        }
        if (msgPageDTO.getShopId() == null) {
            final Long shopId = DistributedContextTools.getShopId();
            msgPageDTO.setShopId(shopId);
        }

        if (msgPageDTO.getAdminId() == null || msgPageDTO.getShopId() == null || msgPageDTO.getBid() == null) {
            logger.error("数据异常,未获取到上下文adminId:{},bid:{},shopId:{}", msgPageDTO.getAdminId(), msgPageDTO.getBid(),
                msgPageDTO.getShopId());
        }
        return pushDelegateService.getPushMsg(msgPageDTO).getData();
    }

}
