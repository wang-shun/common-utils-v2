package com.youzan.sz.common.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.youzan.sz.common.model.BaseDTO;
import com.youzan.sz.common.model.base.BaseStaffDTO;
import com.youzan.sz.common.model.enums.DeviceType;
import com.youzan.sz.common.model.oa.device.DeviceTokenQueryDTO;
import com.youzan.sz.common.util.JsonUtils;
import com.youzan.util.NotThreadSafe;

/**
 *
 * Created by zhanguo on 2016/10/9.
 */
public class PushMsgDTO extends BaseDTO {
    /**
     * 标题
     * */
    private String              title;
    /**
     * 消息内容
     * */
    private String              content;
    /**
     * {@link SendType}
     * */
    @NotNull(message = "发送类型不能为空")

    private Integer             sendType;
    /**
     * 消息类型{@link com.youzan.sz.common.model.enums.MsgTypeEnum}
     * */
    private Integer             msgType;
    private Map<String, String> params;
    /**
     * 接收者
     * */
    private List<String>        recvList = new ArrayList<>(1);
    /**
     * 假设deviceType不为空,则直推送到此设备中
     * @see DeviceType
     * */
    private Integer             deviceType;
    /**批量发送结果*/
    private Map<String, String> result;

    @NotThreadSafe
    public PushMsgDTO addParams(@NotNull String key, @NotNull String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }

    @NotThreadSafe
    /**
     * 批量发送时,如果失败了需要指定错误消息
     * */
    public PushMsgDTO addResult(@NotNull String recvId, @NotNull String value) {
        if (result == null) {
            result = new HashMap<>();
        }
        result.put(recvId, value);
        return this;
    }

    private PushMsgDTO addRecv(String recvInfo) {
        recvList.add(recvInfo);
        return this;
    }

    /**
     * 通过adminId推送
     * */
    public PushMsgDTO addStaffRecv(BaseStaffDTO staffDTO) {
        return addRecv(JsonUtils.toJson(staffDTO));
    }

    /**
     * 通过设备号码推送
     * */
    public PushMsgDTO addDeviceToken(DeviceTokenQueryDTO deviceTokenQueryDTO) {
        return addRecv(JsonUtils.bean2Json(deviceTokenQueryDTO));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSendType() {
        return sendType;
    }

    public void setSendType(Integer sendType) {
        this.sendType = sendType;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public List<String> getRecvList() {
        return recvList;
    }

    public void setRecvList(List<String> recvList) {
        this.recvList = recvList;
    }

    public Map<String, String> getResult() {
        return result;
    }

    public void setResult(Map<String, String> result) {
        this.result = result;
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }
}
