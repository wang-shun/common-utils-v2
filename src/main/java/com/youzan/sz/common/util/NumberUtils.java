package com.youzan.sz.common.util;

import com.youzan.sz.common.client.IdClient;
import com.youzan.sz.common.model.number.NumberTypes;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by zefa on 16/4/9.
 */
public class NumberUtils {
    private static final String idClientHost = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH,
        "idclient.host", "192.168.66.202");
    private static final String idClientPort = PropertiesUtils.getProperty(ConfigsUtils.CONFIG_ENV_FILE_PATH,
        "idclient.port", "6000");

    /**
     * 获取订单 ID
     *
     * @param numberType ID类型
     * @return
     */
    public static String initNumber(NumberTypes numberType) {
        switch (numberType.getInitType()) {
            case snowflake:
                return initSnowflakeNumber(numberType);
            case random:
                return initRandomNumber(numberType);
            case sequences:
                return initSequences(numberType);
            case uuid:
                return initUUID(numberType);
        }
        return "";
    }

    /**
     * 批量获取订单 ID
     *
     * @param numberType ID类型
     * @return
     */
    public static List<String> batchInitNumber(NumberTypes numberType, int num) {
        List<String> result = new ArrayList<>(num);
        switch (numberType.getInitType()) {
            case snowflake:
                result = batchInitSnowflakeNumber(numberType, num);
                break;
            case random:
            case sequences:
            case uuid:
                for (int i = 0; i < num; i++) {
                    result.add(initNumber(numberType));
                }
        }
        return result;
    }

    private static String initSnowflakeNumber(NumberTypes numberType) {
        String timeStamp = new DateTime().toString(numberType.getFormat());
        String randomNumber = RandomUtils.getRandomNumber(numberType.getNumberLength());
        return numberType.getHead() + timeStamp + randomNumber;
    }

    private static List<String> batchInitSnowflakeNumber(NumberTypes numberType, int num) {
        List<String> result = new ArrayList<>(num);
        //首位补1是为了兼容0*年的情况
        String timeStamp = "1" + new DateTime().toString(numberType.getFormat());
        String randomNumber = RandomUtils.getRandomNumber(numberType.getNumberLength());
        Long number = Long.parseLong(timeStamp + randomNumber);
        for (int i = 0; i < num; i++) {
            //去除之前补得首位
            result.add(numberType.getHead() + number.toString().substring(1));
            number++;
        }
        return result;
    }

    private static String initRandomNumber(NumberTypes numberType) {
        return String.valueOf(RandomUtils.getRandomNumber(numberType.getNumberLength()));
    }

    private static String initSequences(NumberTypes numberType) {
        IdClient idClient = new IdClient(idClientHost, Integer.parseInt(idClientPort));
        return String.valueOf(idClient.getId("step", numberType.getHead()));
    }

    private static String initUUID(NumberTypes numberType) {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23)
               + str.substring(24);
    }

    //    public static void main(String args[]) {
    //        System.out.println(NumberTypes.PRODUCT.getName() + ":" + NumberUtils.initNumber(NumberTypes.PRODUCT));
    //        System.out.println(NumberTypes.SKU.getName() + ":" + NumberUtils.initNumber(NumberTypes.SKU));
    //        System.out.println(NumberTypes.SELL.getName() + ":" + NumberUtils.initNumber(NumberTypes.SELL));
    //        System.out.println(NumberTypes.RETURN.getName() + ":" + NumberUtils.initNumber(NumberTypes.RETURN));
    //        System.out.println(NumberTypes.STOCKCHECK.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKCHECK));
    //        System.out.println(NumberTypes.PURCHASE.getName() + ":" + NumberUtils.initNumber(NumberTypes.PURCHASE));
    //        System.out.println(NumberTypes.STOCKIN.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKIN));
    //        System.out.println(NumberTypes.STOCKOUT.getName() + ":" + NumberUtils.initNumber(NumberTypes.STOCKOUT));
    //        System.out.println(NumberTypes.SHOUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.SHOUKUAN));
    //        System.out.println(NumberTypes.FUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.FUKUAN));
    //        System.out.println(NumberTypes.YINGSHOUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.YINGSHOUKUAN));
    //        System.out.println(NumberTypes.YINGFUKUAN.getName() + ":" + NumberUtils.initNumber(NumberTypes.YINGFUKUAN));
    //        System.out.println(NumberTypes.STAFFBINDID.getName() + ":" + NumberUtils.initNumber(NumberTypes.STAFFBINDID));
    //        System.out.println(NumberTypes.BANKACCOUNTID.getName() + ":" + NumberUtils.initNumber(NumberTypes.BANKACCOUNTID));
    //        System.out.println(NumberTypes.SPNODEID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SPNODEID));
    //        System.out.println(NumberTypes.CATEGORYID.getName() + ":" + NumberUtils.initNumber(NumberTypes.CATEGORYID));
    //        System.out.println(NumberTypes.SHOPID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SHOPID));
    //        System.out.println(NumberTypes.ACCOUNT.getName() + ":" + NumberUtils.initNumber(NumberTypes.ACCOUNT));
    //        System.out.println(NumberTypes.PRODUCTID.getName() + ":" + NumberUtils.initNumber(NumberTypes.PRODUCTID));
    //        System.out.println(NumberTypes.SKUID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SKUID));
    //        System.out.println("批量方法");
    //        NumberUtils.testBatch(NumberTypes.SELL, 10);
    //    }

    public static void testBatch(NumberTypes numberType, int num) {
        List<String> ids = batchInitNumber(numberType, num);
        System.out.println(numberType.getName());
        for (String str : ids) {
            System.out.println(str);
        }
    }

    public static boolean isPositive(Number number) {
        return number != null && number.longValue() > 0;
    }

    public static boolean isNotPositive(Number number) {
        return !isPositive(number);
    }

    public static <T extends Number> boolean isNotBetween(T min, T max, T current) {
        return !isBetween(min, max, current);
    }

    public static <T extends Number> boolean isBetween(T min, T max, T current) {
        if (current == null) {
            return false;
        }
        if (min instanceof Integer) {
            return min.intValue() < current.intValue() && max.intValue() > current.intValue();
        }
        //向大的转换,不丢失精度
        return min.longValue() < current.longValue() && max.longValue() > current.longValue();

    }

    public static void main(String[] args) throws Throwable {
        for (int i = 0; i < 100; i++) {
            System.out.println(NumberTypes.PRODUCTID.getName() + ":" + NumberUtils.initNumber(NumberTypes.PRODUCTID));
            for (int k = 0; k < 5; k++) {
                System.out.println(NumberTypes.SKUID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SKUID));
            }
        }

    }
}
