package com.youzan.sz.dqueue.codec.json;

import com.youzan.sz.common.util.JsonUtils;
import com.youzan.sz.dqueue.codec.Decode;

/**
 * Created by wangpan on 2016/9/30.
 */
public class StringDecode implements Decode {
    
    @Override
    public <T> T decode(String response, Class<T> z) {
        return JsonUtils.json2Bean(response, z);
    }
}
