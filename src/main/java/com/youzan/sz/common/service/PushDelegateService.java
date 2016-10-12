package com.youzan.sz.common.service;

import com.youzan.sz.common.SignOut;
import com.youzan.sz.common.model.Page;
import com.youzan.sz.common.push.PushMsgDTO;
import com.youzan.sz.common.push.PushContextDTO;
import com.youzan.sz.common.push.msg.MsgDTO;
import com.youzan.sz.common.push.msg.MsgPageDTO;
import com.youzan.sz.common.push.msg.MsgReadDTO;
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

    /**
     * @return 返回推送成功的接收者
     * */
    @SignOut
    BaseResponse<PushMsgDTO> pushMsg(PushMsgDTO pushMsgDTO);

    /**
     *拉取消息列表 
     * */
    BaseResponse<Page<MsgDTO>> getPushMsg(MsgPageDTO msgPageDTO);

    /**
     * 标记消息为已读
     * */
    BaseResponse<MsgReadDTO> markRead(MsgReadDTO msgReadDTO);
}
