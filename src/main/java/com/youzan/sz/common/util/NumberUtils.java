package com.youzan.sz.common.util;

import com.youzan.sz.common.client.IdClient;
import com.youzan.sz.common.model.number.InitType;
import com.youzan.sz.common.model.number.NumberTypes;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Created by zefa on 16/4/9.
 */
public class NumberUtils {
    private static final String propFileName = "application.properties";
    private static final String idClientHost = PropertiesUtils.getProperty(propFileName,"idclient.host","192.168.66.202");
    private static final String idClientPort = PropertiesUtils.getProperty(propFileName,"idclient.port","6000");

    /**
     * 获取订单 ID
     * @param numberType ID类型
     * @return
     */
    public static String initNumber(NumberTypes numberType){
        switch (numberType.getInitType()){
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
     * @param numberType ID类型
     * @return
     */
    public static List<String> batchInitNumber(NumberTypes numberType, int num) {
        List<String> result = new ArrayList<>(num);
        switch (numberType.getInitType()){
            case snowflake:
                result = batchInitSnowflakeNumber(numberType,num);
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
        int randomNumber = RandomUtils.getRandomNumber(numberType.getNumberLength());
        return numberType.getHead() + timeStamp + (randomNumber > 0 ? randomNumber : "");
    }

    private static List<String> batchInitSnowflakeNumber(NumberTypes numberType, int num) {
        List<String> result = new ArrayList<>(num);
        String timeStamp = new DateTime().toString(numberType.getFormat());
        long millTimes = Long.parseLong(timeStamp);
        int randomNumber = RandomUtils.getRandomNumber(numberType.getNumberLength());
        long number = millTimes*10*numberType.getNumberLength() + randomNumber;
        for (int i = 0; i < num; i++) {
            result.add(numberType.getHead() + number);
            number++;
        }
        return result;
    }

    private static String initRandomNumber(NumberTypes numberType){
        return String.valueOf(RandomUtils.getRandomNumber(numberType.getNumberLength()));
    }

    private static String initSequences(NumberTypes numberType){
        IdClient idClient = new IdClient(idClientHost,Integer.parseInt(idClientPort));
        return String.valueOf(idClient.getId("step",numberType.getHead()));
    }

    private static String initUUID(NumberTypes numberType) {
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        // 去掉"-"符号
        return str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
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
        System.out.println(NumberTypes.SKU.getName() + ":" + NumberUtils.initNumber(NumberTypes.SKU));
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
        System.out.println(NumberTypes.STAFFBINDID.getName() + ":" + NumberUtils.initNumber(NumberTypes.STAFFBINDID));
        System.out.println(NumberTypes.BANKACCOUNTID.getName() + ":" + NumberUtils.initNumber(NumberTypes.BANKACCOUNTID));
        System.out.println(NumberTypes.SPNODEID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SPNODEID));
        System.out.println(NumberTypes.CATEGORYID.getName() + ":" + NumberUtils.initNumber(NumberTypes.CATEGORYID));
        System.out.println(NumberTypes.SHOPID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SHOPID));
        System.out.println(NumberTypes.ACCOUNT.getName() + ":" + NumberUtils.initNumber(NumberTypes.ACCOUNT));
        System.out.println(NumberTypes.PRODUCTID.getName() + ":" + NumberUtils.initNumber(NumberTypes.PRODUCTID));
        System.out.println(NumberTypes.SKUID.getName() + ":" + NumberUtils.initNumber(NumberTypes.SKUID));
    }
}
