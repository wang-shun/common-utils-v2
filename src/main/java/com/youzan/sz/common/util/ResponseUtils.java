package com.youzan.sz.common.util;

import com.youzan.sz.common.response.ResponseBoolean;

/**
 * Created by zefa on 16/7/7.
 */
public class ResponseUtils {
    private ResponseUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static final ResponseBoolean TRUE = new ResponseBoolean(Boolean.TRUE);
    public static final ResponseBoolean FALSE = new ResponseBoolean(Boolean.FALSE);

    public static ResponseBoolean getResponseBoolean(boolean result){
        return result ? TRUE : FALSE;
    }
}
