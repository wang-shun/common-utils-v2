package com.youzan.sz.common.cache;

/**
 *
 * Created by zhanguo on 16/7/20.
 * 具体规范参考 <a href="http://doc.qima-inc.com/pages/viewpage.action?pageId=5771750">规范</a>
 */
public interface ICacheKey {

    String getBaseKey();

    default String combineKey(String... keys) {
        StringBuilder sb = new StringBuilder(getBaseKey());
        if (keys != null && keys.length > 0) {
            for (String key : keys) {
                if (key == null || key.length() == 0) {
                    throw new NullPointerException(key);
                }
                sb.append(":").append(key.trim());
            }
        }
        return sb.toString();
    }
}
