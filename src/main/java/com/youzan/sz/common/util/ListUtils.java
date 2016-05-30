package com.youzan.sz.common.util;

import java.util.ArrayList;
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
