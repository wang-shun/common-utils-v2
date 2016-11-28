package com.youzan.sz.common.util;

import com.youzan.sz.common.Common;

import java.math.BigDecimal;

/**
 * Created by YANG on 16/4/22.
 */
public class PriceUtil {

    private PriceUtil() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 分转元,100分 = 1元
     *
     * @param price
     * @return
     */
    public static String convertF2Y(Long price) {
        return new BigDecimal(price).divide(new BigDecimal(100F)).toString();
    }

    /**
     * 计算价格 (单价 * 新的数量)
     *
     * @param price
     * @param amount
     * @return
     */
    public static BigDecimal totalPrice(Long price, Integer amount) {
        return new BigDecimal(price * amount).divide(new BigDecimal(Common.QUANTITY_MULTIPLE));
    }

    /**
     * 计算价格四舍五入 (单价 * 新的数量)
     *
     * @param price
     * @param amount
     * @return
     */
    public static Long totalRoundingPrice(Long price, Integer amount) {
        return  totalPrice(price, amount).setScale(0,BigDecimal.ROUND_HALF_UP).longValue();
    }

    public static void main(String[] args) {
//        System.out.println(new BigDecimal(0.5).setScale(0,BigDecimal.ROUND_HALF_UP).longValue());
//        System.out.println(new BigDecimal(0.5).setScale(0,BigDecimal.ROUND_HALF_UP));
        System.out.println("totalPrice  " + totalPrice(100L,5));
        System.out.println("totalRoundingPrice  " + totalRoundingPrice(100L,5));
    }
}
