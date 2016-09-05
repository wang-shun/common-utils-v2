package com.youzan.sz.common.push;

import com.youzan.platform.courier.common.MsgChannel;

/**
 *
 * Created by zhanguo on 16/8/28.
 * 
 * 
 */
public class SmsVerifyPushConfigDTO extends PushConfigDTO {
    public enum VerifyMode {
                            NUMERIC, ALPHABET;
    }

    private int        ttl   = 120;                //有效生存时间
    private VerifyMode mode  = VerifyMode.NUMERIC; //1->默认为数字
    private int        count = 4;                  //默认为4位

    public SmsVerifyPushConfigDTO() {
        this.msgChannel = MsgChannel.sms;
    }

    public int getTtl() {
        return ttl;
    }

    public SmsVerifyPushConfigDTO setTtl(int ttl) {
        this.ttl = ttl;
        return this;
    }

    public VerifyMode getMode() {
        return mode;
    }

    public SmsVerifyPushConfigDTO setMode(VerifyMode mode) {
        this.mode = mode;
        return this;
    }

    public int getCount() {
        return count;
    }

    public SmsVerifyPushConfigDTO setCount(int count) {
        this.count = count;
        return this;
    }

}
