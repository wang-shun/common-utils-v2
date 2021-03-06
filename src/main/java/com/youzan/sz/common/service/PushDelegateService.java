package com.youzan.sz.common.service;

import com.youzan.sz.common.SignOut;
import com.youzan.sz.common.model.Page;
import com.youzan.sz.common.push.PushContextDTO;
import com.youzan.sz.common.push.PushMsgDTO;
import com.youzan.sz.common.push.msg.MsgDTO;
import com.youzan.sz.common.push.msg.MsgPageDTO;
import com.youzan.sz.common.push.msg.MsgReadDTO;
import com.youzan.sz.common.response.BaseResponse;

import java.util.List;


/**
 *
 * Created by zhanguo on 16/8/28.
 */
public interface PushDelegateService {
    
    /**
     * 拉取消息列表
     */
    BaseResponse<Page<MsgDTO>> getPushMsg(MsgPageDTO msgPageDTO);
    
    /**
     * 幂等,校验票据,不删除
     * 命名方法参考{@link Thread#isInterrupted()} vs{@link Thread#interrupted()}
     */
    BaseResponse<PushContextDTO> isValid(PushContextDTO pushContextDTO);
    
    /**
     * 标记消息为已读
     */
    BaseResponse<MsgReadDTO> markRead(MsgReadDTO msgReadDTO);

    BaseResponse push(PushContextDTO pushContextDTO);

    /**
     * @return 返回推送成功的接收者
     * */
    @SignOut
    BaseResponse<PushMsgDTO> pushMsg(PushMsgDTO pushMsgDTO);
    
    /**
     * 发送验证码
     * */
    BaseResponse<PushContextDTO> pushVerifyCode(PushContextDTO pushContextDTO);

    /**
     * 校验,成功后会销毁凭据
     * */
    BaseResponse<PushContextDTO> valid(PushContextDTO pushContextDTO);
    
    /**
     * 幂等,校验票据,不删除
     * @deprecated {@link PushDelegateService#isValid(PushContextDTO)}
     * */
    @Deprecated
    BaseResponse<PushContextDTO> validate(PushContextDTO pushContextDTO);

    /**
     *
     *@deprecated {@link PushDelegateService#valid(PushContextDTO)}
     * */
    @Deprecated
    BaseResponse<PushContextDTO> validateWithDisposal(PushContextDTO pushContextDTO);
    
    
    /**
     * 删除通知
     * @param msgReadDTO
     * @return
     */
    BaseResponse<List<String>> removeMsg(MsgReadDTO msgReadDTO);
}
