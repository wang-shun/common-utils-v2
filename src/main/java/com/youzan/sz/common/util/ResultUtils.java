package com.youzan.sz.common.util;

import com.youzan.sz.common.model.Result;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/2.
 */
public class ResultUtils {
    public static Result getSuccessResult(Object data) {
        Result result = new Result();
        result.setData(data);
        result.setCode(0);
        return result;
    }

    public static Result getFailResult(ResponseCode responseCode) {
        Result result = new Result();
        result.setCode(responseCode.getCode());
        result.setMsg(responseCode.getMessage());
        return result;
    }

}
