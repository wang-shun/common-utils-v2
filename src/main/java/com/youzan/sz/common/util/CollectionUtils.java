package com.youzan.sz.common.util;

import java.util.*;

/**
 *
 * Created by zhanguo on 16/8/15.
 */
public class CollectionUtils {
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isNotEmpty(Iterable iterable) {
        return !isEmpty(iterable);
    }

    public static boolean isEmpty(Iterable iterable) {
        if (iterable != null) {
            return iterable.iterator().hasNext();
        }
        return false;
    }

    public static boolean isEmpty(Map map) {
        if (map == null) {
            return true;
        }
        return map.isEmpty();
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

}
