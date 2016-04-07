package com.youzan.sz.common.util;

import java.util.List;

/**
 * Created by zefa on 16/3/30.
 */
public class ListUtils {
    /**
     *
     * @param list
     * @return
     */
    public static String list2String(List list){
        String result = "";
        for (Object object: list){
            result += String.valueOf(object) + ",";
        }
        if (result.length() > 0){
            result = result.substring(0,result.length() - 1);
        }
        return result;
    }
}
