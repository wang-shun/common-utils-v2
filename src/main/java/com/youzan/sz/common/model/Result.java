package com.youzan.sz.common.model;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class Result {
    private Integer code;
    private Object  data;
    private String  msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
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
        return !(code != null && Integer.valueOf(0).equals(code));

    }

}
