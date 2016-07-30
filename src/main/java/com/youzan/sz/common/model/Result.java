package com.youzan.sz.common.model;

/**
 *
 * Created by zhanguo on 16/7/29.
 */
public class Result {
    private Integer ret; //0成功,1失败;
    private Object  data;
    private String  msg;

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
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

    public boolean isSucc() {
        return ret == null || ret.intValue() == 0;
    }

}
