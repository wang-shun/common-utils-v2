package com.youzan.sz.common.util;

import java.util.Collection;

/**
 *
 * Created by zhanguo on 16/8/15.
 */
public class CollectionUtils {
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
