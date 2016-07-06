package com.youzan.sz.common.client;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.courier.api.PushService;
import com.youzan.platform.courier.common.MsgChannel;
import com.youzan.platform.courier.domain.MessageContext;
import com.youzan.platform.courier.domain.Recipient;
import com.youzan.sz.common.Common;
import com.youzan.sz.common.model.enums.DeviceType;
import com.youzan.sz.common.response.enums.ResponseCode;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.common.util.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zefa on 16/7/5.
 */
public class CourierClient {
    private static CourierClient instance = null;
    private static boolean isInit = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(CourierClient.class);

    private static final String IOS_PUSH = "apns";
    private static final String ANDROID_PUSH = "gpns";

    public static CourierClient getInstance() {
        // 判断session服务是否已经已经具备使用条件了
        if (!isInit) {
            LOGGER.warn("the push service not ready, so can not use!");
            return null;
        }
        if (null == instance) {
            synchronized (CourierClient.class) {
                if (null == instance) {
                    try {
                        instance = new CourierClient();
                    } catch (Exception e) {
                        e.printStackTrace();
                        isInit = false;
                    }
                }

            }
        }
        return instance;
    }


    private PushService pushService = SpringUtils.getBean(PushService.class);

    private CourierClient() {
        // 依赖的服务不存在，则session服务不具备使用条件，不能使用
        if (null == pushService) {
            throw new BusinessException((long) ResponseCode.SESSION_SERVICE_NOT_EXIST.getCode(),
                    ResponseCode.SESSION_SERVICE_NOT_EXIST.getMessage());
        }
    }

    /**
     * 推送单个设备
     * @param content 推送内容
     * @param title 标题
     * @param uri 业务类型,与app约定
     * @param deviceType 设备类型
     * @param deviceId 设备编号
     * @param templateName 模板名称(消息组注册)
     * @param param 详情(与app约定好的json)
     */
    public void singlePush( String content, String title, String uri, DeviceType deviceType, String deviceId, String templateName, Map<String,String> param){
        Map<String, String> params = new HashMap<>();
        params.put("app", Common.APPNAME);
        params.put("role", "device");
        params.put("content", title);
        params.put("title", content);
        params.put("badge", "1");
        params.put("uri", uri);
        param.putAll(params);
        Map<String, String> receiverMap = buildReceiverMap(deviceType,deviceId);
        MessageContext messageContext = new MessageContext(templateName, params);
        Recipient recipient = new Recipient("", JsonUtils.bean2Json(receiverMap), MsgChannel.appPush);
        pushService.sendMessage(messageContext, recipient);
    }

    private Map<String, String> buildReceiverMap(DeviceType deviceType, String deviceId){
        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put("token", deviceId);
        receiverMap.put("type", getReceiverType(deviceType));
        return receiverMap;
    }

    private String getReceiverType(DeviceType deviceType){
        switch (deviceType){
            case IPAD:
                return IOS_PUSH;
            case POS:
                return ANDROID_PUSH;
            default:
                return "";
        }
    }
}
