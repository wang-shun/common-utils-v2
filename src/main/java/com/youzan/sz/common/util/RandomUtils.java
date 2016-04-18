package com.youzan.sz.common.util;

import java.util.Random;

/**
 * Created by zefa on 16/4/18.
 */
public class RandomUtils {
    /**
     * 获取指定位数的随机数
     *
     * @param figure 随机数位数
     * @return 随机数
     */
    public static int getRandomNumber(int figure) {
        return (int) ((1 + new Random().nextDouble()) * Math.pow(10, figure - 1));
    }
    public static void main(String[] args) {
        System.out.println(RandomUtils.getRandomNumber(6));
    }
}
