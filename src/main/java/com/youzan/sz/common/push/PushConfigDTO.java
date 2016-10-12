package com.youzan.sz.common.push;

import com.youzan.platform.courier.common.MsgChannel;
import org.hibernate.validator.constraints.NotEmpty;

import com.youzan.sz.common.model.BaseDTO;

/**
 *
 * Created by zhanguo on 16/8/28.
 * 
 * 
 */
public class PushConfigDTO extends BaseDTO {
    @NotEmpty(message = "必须指定模板名字")
    protected String     templateName;
    /**
     * 替换参数名字
     * */
    private String       credentialsName = "credentials";

    /***为了避免重复,最好添加自己的应用名在前面.例如:portal_reg**/
    @NotEmpty(message = "必须指定应用key")
    protected String     appKey;
    //    @NotNull(message = "必须指定发送渠道")
    protected MsgChannel msgChannel;

    public String getTemplateName() {
        return templateName;
    }

    public PushConfigDTO setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getAppKey() {
        return appKey;
    }

    public PushConfigDTO setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public String getCredentialsName() {
        return credentialsName;
    }

    public PushConfigDTO setCredentialsName(String credentialsName) {
        this.credentialsName = credentialsName;
        return this;
    }

    public MsgChannel getMsgChannel() {
        return msgChannel;
    }

    public PushConfigDTO setMsgChannel(MsgChannel msgChannel) {
        this.msgChannel = msgChannel;
        return this;
    }
}
