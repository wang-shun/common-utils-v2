package com.youzan.sz.dqueue.codec.json;

import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.dqueue.codec.Encode;

/**
 * Created by wangpan on 2016/9/30.
 */
public class StringEncode implements Encode {
    @Override
    public String encode(Object object) {
        return JsonUtils.bean2Json(object);
    }
}
