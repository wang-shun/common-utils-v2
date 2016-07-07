package com.youzan.sz.common.util;

import com.youzan.sz.common.response.ResponseBoolean;

/**
 * Created by zefa on 16/7/7.
 */
public final class ResponseUtils {
    private ResponseUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static final ResponseBoolean TRUE_RESPONSE = new ResponseBoolean(Boolean.TRUE);
    public static final ResponseBoolean FALSE_RESPONSE = new ResponseBoolean(Boolean.FALSE);

    public static ResponseBoolean getResponseBoolean(boolean result) {
        return result ? TRUE_RESPONSE : FALSE_RESPONSE;
    }
}
