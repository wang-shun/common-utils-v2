package com.youzan.sz.common.util;

import com.youzan.sz.common.model.number.NumberTypes;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zefa on 16/4/9.
 */
public class NumberUtils {
    public static String initNumber(NumberTypes numberType) {
        String timeStamp = new DateTime().toString(numberType.getFormat());
        int randomNumber = (int) ((1 + new Random().nextDouble()) * Math.pow(10, numberType.getNumberLength() - 1));
        return numberType.getHead() + timeStamp + (randomNumber > 0 ? randomNumber : "");
    }

    public static List<String> batchGenerateSkuNo(int num) {
        String timeStamp = DateTime.now().toString(NumberTypes.PRODUCT.getFormat());
        long millTimes = Long.parseLong(timeStamp);
        StringBuilder sb = new StringBuilder();
        List<String> result = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            result.add(sb.append(NumberTypes.PRODUCT.getHead()).append(millTimes).toString());
            sb.delete(0, sb.length());
            millTimes++;
        }
        return result;
    }

    public static void main(String args[]) {
        System.out.println(NumberTypes.PRODUCT.getName() + ":" + NumberUtils.initNumber(NumberTypes.PRODUCT));
        System.out.println(NumberTypes.SELL.getName() + ":" + NumberUtils.initNumber(NumberTypes.SELL));
        System.out.println(NumberTypes.RETURN.getName() + ":" + NumberUtils.initNumber(NumberTypes.RETURN));
        System.out.println(NumberTypes.STOCKCHECK.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKCHECK));
        System.out.println(NumberTypes.PURCHASE.getName() + ":" + NumberUtils.initNumber(NumberTypes.PURCHASE));
        System.out.println(NumberTypes.STOCKIN.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKIN));
        System.out.println(NumberTypes.STOCKOUT.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKOUT));
        System.out.println(NumberTypes.SHOUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.SHOUKUAN));
        System.out.println(NumberTypes.FUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.FUKUAN));
        System.out.println(NumberTypes.YINGSHOUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.YINGSHOUKUAN));
        System.out.println(NumberTypes.YINGFUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.YINGFUKUAN));

    }
}
