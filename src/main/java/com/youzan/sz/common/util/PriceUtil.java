package com.youzan.sz.common.util;

import java.math.BigDecimal;

/**
 * Created by YANG on 16/4/22.
 */
public class PriceUtil {

    /**
     * 分转元,100分 = 1元
     *
     * @param price
     * @return
     */
    public static String convertF2Y(Long price) {
        return new BigDecimal(price).divide(new BigDecimal(100F)).toString();
    }
}
