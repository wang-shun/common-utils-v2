package com.youzan.sz.common.exceptions;

import com.youzan.platform.bootstrap.exception.BusinessException;
import com.youzan.sz.common.response.enums.ResponseCode;

/**
 *
 * Created by zhanguo on 16/8/15.
 */
public class BizException extends BusinessException {

    public final static BizException APP_NOT_SUPPORT = new BizException(ResponseCode.APP_NOT_SUPPORT);

    private Object                   data;

    public BizException() {
    }

    public BizException(Throwable throwable) {
        super(throwable);
    }

    public BizException(String message) {
        super(message);
    }

    public BizException(Long code, String message) {
        super(code, message);
    }

    public BizException(Long code, String message, Throwable throwable) {
        super(code, message, throwable);
    }

    public BizException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public BizException(ResponseCode responseCode, Object data) {
        this(responseCode, responseCode.getMessage(), data);
    }

    public BizException(ResponseCode responseCode) {
        this(responseCode, responseCode.getMessage(), null);
    }

    /**
     *@param data  抛出错误时需要返回的数据
     *             @param responseCode 返回码
     * */
    public BizException(ResponseCode responseCode, String message, Object data) {
        this((long) responseCode.getCode(), message == null ? responseCode.getMessage() : message);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
