package com.youzan.sz.common.model;

import java.io.Serializable;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class Result<T> implements Serializable {
    private Integer code;
    private T       data;
    private String  msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     *code存在,且不为0时算作失败.其他条件都是成功
     * */
    public boolean isSucc() {
        return code == null || Integer.valueOf(0).equals(code);

    }

}
