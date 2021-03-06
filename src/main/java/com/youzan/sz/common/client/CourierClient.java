package com.youzan.sz.common.client;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.platform.push.api.PushService;
import com.youzan.platform.courier.common.MsgChannel;
import com.youzan.platform.push.domain.MessageContext;
import com.youzan.platform.push.domain.Recipient;
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
public final class CourierClient {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CourierClient.class);
    
    private static final String IOS_PUSH = "apns";
    
    private static final String ANDROID_PUSH = "gpns";
    
    private static volatile CourierClient instance = null;
    
    private static boolean isInit = true;
    
    private PushService pushService = SpringUtils.getBean(PushService.class);
    
    
    private CourierClient() {
        // 依赖的服务不存在，则session服务不具备使用条件，不能使用
        if (null == pushService) {
            throw new BusinessException((long) ResponseCode.PUSH_SERVICE_NOT_EXIST.getCode(), ResponseCode.PUSH_SERVICE_NOT_EXIST.getMessage());
        }
    }
    
    
    public static CourierClient getInstance() {
        // 判断push服务是否已经已经具备使用条件了
        if (!isInit) {
            LOGGER.warn("the push service not ready, so can not use!");
            throw new BusinessException((long) ResponseCode.PUSH_SERVICE_NOT_EXIST.getCode(), ResponseCode.PUSH_SERVICE_NOT_EXIST.getMessage());
        }
        if (null == instance) {
            synchronized (CourierClient.class) {
                if (null == instance) {
                    try {
                        instance = new CourierClient();
                    } catch (BusinessException e) {
                        LOGGER.warn("推送异常:{}", e);
                        isInit = false;
                        throw e;
                    }
                }
            }
        }
        return instance;
    }
    
    
    /**
     * 推送单个设备
     *
     * @param content 推送内容
     * @param title 标题
     * @param uri 业务类型,与app约定
     * @param deviceType 设备类型
     * @param pushId 设备编号
     * @param templateName 模板名称(消息组注册)
     * @param param 详情(与app约定好的json)
     */
    public void singlePush(String content, String title, String uri, DeviceType deviceType, String pushId, String templateName, Map<String, String> param) {
        Map<String, String> params = new HashMap<>();
        params.put("app", "cashier");
        params.put("role", "device");
        params.put("content", content);
        params.put("title", title);
        params.put("badge", "1");
        params.put("uri", uri);
        params.putAll(param);
        Map<String, String> receiverMap = buildReceiverMap(deviceType, pushId);
        MessageContext messageContext = new MessageContext(templateName, params);
        Recipient recipient = new Recipient("", JsonUtils.bean2Json(receiverMap), MsgChannel.appPush);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("给 {} 设备发送一条推送,templateName: {},Receiver:{}, 参数:{}", pushId, templateName, JsonUtils.bean2Json(receiverMap), JsonUtils.bean2Json(params));
        }
        String response = "";
        try {
            pushService.sendMessage(messageContext, recipient);
        } catch (Exception e) {
            LOGGER.warn("推送信息给设备出现问题:{}", e);
            throw new BusinessException((long) ResponseCode.PUSH_DEVICE_INFO.getCode(), ResponseCode.PUSH_DEVICE_INFO.getMessage(), e);
        }
        
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("App推送消息结果:{}", response);
        }
    }
    
    
    private Map<String, String> buildReceiverMap(DeviceType deviceType, String pushId) {
        Map<String, String> receiverMap = new HashMap<>();
        receiverMap.put("token", pushId);
        receiverMap.put("type", getReceiverType(deviceType));
        return receiverMap;
    }
    
    
    private String getReceiverType(DeviceType deviceType) {
        switch (deviceType) {
            case IPAD:
            case IPHONE:
                return IOS_PUSH;
            case POS:
            case APAD:
            case ANDROID:
                return ANDROID_PUSH;
            default:
                return "";
        }
    }
}
