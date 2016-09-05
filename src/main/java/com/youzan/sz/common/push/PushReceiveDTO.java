package com.youzan.sz.common.push;

import com.youzan.sz.common.model.BaseDTO;
import com.youzan.sz.common.model.enums.ResultEnum;

/**
 *
 * Created by zhanguo on 16/8/28.
 *
 */
public class PushReceiveDTO extends BaseDTO {
    private String receiver;
    /**生成的票据*/
    private String credential;
    private int    verifyResult = ResultEnum.FAIL.getCode();//1是成功,2是失败

    public PushReceiveDTO() {
    }

    public String getReceiver() {
        return receiver;
    }

    public PushReceiveDTO setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public String getCredential() {
        return credential;
    }

    public PushReceiveDTO setCredential(String credential) {
        this.credential = credential;
        return this;
    }

    public int getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(int verifyResult) {
        this.verifyResult = verifyResult;
    }
}
