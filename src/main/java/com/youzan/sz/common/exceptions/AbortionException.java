package com.youzan.sz.common.exceptions;

import com.youzan.platform.bootstrap.exception.BusinessException;

/**
 *
 * Created by zhanguo on 2016/9/27.
 */
public class AbortionException extends BusinessException {
    public static final AbortionException ABORTION_EXCEPTION = new AbortionException();
}
