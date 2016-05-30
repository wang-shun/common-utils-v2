package com.youzan.sz.common.util;

import java.util.Random;

/**
 * Created by zefa on 16/4/18.
 */
public class RandomUtils {

    private static final Random RANDOM = new Random();

    /**
     * 获取指定位数的随机数
     * (首位1是防止00023变成23导致位数不足,所以请自行去除)
     *
     * @param figure 随机数位数
     * @return 随机数
     */
    public static int getRandom(int figure) {
        return (int) ((1 + RANDOM.nextDouble()) * Math.pow(10, figure));
    }

    /**
     * 获取指定位数的随机数
     *
     * @param figure 随机数位数
     * @return 随机数
     */
    public static String getRandomNumber(int figure) {
        int random = getRandom(figure);
        return String.valueOf(random).substring(1);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++)
            System.out.println(RandomUtils.getRandomNumber(0));
    }
}
