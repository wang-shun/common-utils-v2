package com.youzan.sz.common.util;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zefa on 16/3/30.
 */
public class ListUtils {
    /**
     * @param list
     * @return
     */
    public static String list2String(List list) {
        StringBuilder result = new StringBuilder();
        for (Object object : list) {
            result.append(String.valueOf(object));
            result.append(",");
        }
        if (result.length() > 0) {
            return result.substring(0, result.length() - 1);
        }
        return result.toString();
    }

    public static String join(Iterator iterator, String separator) {

        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return ObjectUtils.toString(first);
        }

        // two or more elements
        StringBuffer buf = new StringBuffer(256);
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * 比较两个list的不同之处
     *
     * @param firstList
     * @param secondList
     * @param <E>
     * @return 第一个比第二个多的
     */
    public static <E> List<E> compareTheDifferenceBetween2List(List<E> firstList, List<E> secondList) {
        List<E> differenceList = new ArrayList<>(firstList);
        for (E elem : secondList) {
            if (firstList.contains(elem))
                differenceList.remove(elem);
        }
        return differenceList;
    }

    /**
     * 比较两个list的相同之处
     *
     * @param firstList
     * @param secondList
     * @param <E>
     * @return
     */
    public static <E> List<E> comparisonOfTheTwoListInTheSamePlace(List<E> firstList, List<E> secondList) {
        List<E> differenceList = new ArrayList<>(firstList);
        for (E elem : secondList) {
            if (!firstList.contains(elem))
                differenceList.remove(elem);
        }
        return differenceList;
    }
}
