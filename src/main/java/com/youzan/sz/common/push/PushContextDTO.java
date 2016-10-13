package com.youzan.sz.common.push;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.youzan.platform.courier.domain.Recipient;
import org.hibernate.validator.constraints.NotEmpty;

import com.youzan.sz.common.model.BaseDTO;

/**
 *
 * Created by zhanguo on 16/8/28.
 * 推送内容
 */
public class PushContextDTO extends BaseDTO {
    @NotNull(message = "需要指定配置文件")
    private PushConfigDTO        pushConfigDTO;

    private Map<String, String>  params;
    @NotEmpty(message = "消息接收者不能为空")
    private List<PushReceiveDTO> recvList;

    private List<Recipient>      recipientList;

    public Map<String, String> getParams() {
        return params;
    }

    public PushContextDTO setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public PushConfigDTO getPushConfigDTO() {
        return pushConfigDTO;
    }

    public PushContextDTO setPushConfigDTO(PushConfigDTO pushConfigDTO) {
        this.pushConfigDTO = pushConfigDTO;
        return this;

    }

    public List<Recipient> getRecipientList() {
        return recipientList;
    }

    public void setRecipientList(List<Recipient> recipientList) {
        this.recipientList = recipientList;
    }

    public List<PushReceiveDTO> getRecvList() {
        return recvList;
    }

    public PushContextDTO setRecvList(List<PushReceiveDTO> recvList) {
        this.recvList = recvList;
        return this;
    }
}
