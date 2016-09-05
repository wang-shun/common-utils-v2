package com.youzan.sz.common.push;

import com.youzan.platform.courier.common.MsgChannel;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/28.
 *     ls,
 pf,
 wxdkf,
 oldwxdkf,
 wsc,
 fx,
 sms,
 wechat,
 dkf,
 appPush,
 mail;
 */
public enum PushChannel {
                         SMS, EMAIL, WX;

    public static MsgChannel getChannel(String receiver) {
        final PushChannel pushChannel = valueOf(receiver);
        switch (pushChannel) {
            case SMS:
                return MsgChannel.sms;
            case EMAIL:
                return MsgChannel.mail;
            case WX:
                return MsgChannel.wechat;
            default:
                throw ResponseCode.PARAMETER_ERROR.getBusinessException("不支持的类型:" + receiver);
        }
    }
}
