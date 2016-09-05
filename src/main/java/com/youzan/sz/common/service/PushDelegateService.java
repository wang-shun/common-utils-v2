package com.youzan.sz.common.service;

import com.youzan.sz.common.push.PushContextDTO;
import com.youzan.sz.common.response.BaseResponse;

/**
 *
 * Created by zhanguo on 16/8/28.
 */
public interface PushDelegateService {

    BaseResponse push(PushContextDTO pushContextDTO);

    /**
     * 发送验证码
     * */
    BaseResponse<PushContextDTO> pushVerifyCode(PushContextDTO pushContextDTO);

    /**
     * 幂等,校验票据,不删除
     * */
    BaseResponse<PushContextDTO> validate(PushContextDTO pushContextDTO);

    /**
     * 校验,会销毁凭据
     * */
    BaseResponse<PushContextDTO> validateWithDisposal(PushContextDTO pushContextDTO);

    //    BaseResponse sendSmsVerify(String appKey, String phone, Integer ttl, Integer count);
    //
    //    BaseResponse smsVerify(String appKey, String phone, String credentials);
}
