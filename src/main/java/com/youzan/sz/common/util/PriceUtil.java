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
    public static BigDecimal totalPrice(Long price, Long amount) {
        return new BigDecimal(price * amount).divide(new BigDecimal(Common.QUANTITY_MULTIPLE));
    }

}
