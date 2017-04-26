package com.youzan.sz.common.util;

import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by heshen on 2017/4/26.
 */
public class MdcUtil {
    
    /**
     * 时间戳+随机数生成唯一id，用于标识唯一请求
     */
    public static String createMDCTraceId() {
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime % 1000000L;
        int randomNumber = ThreadLocalRandom.current().nextInt(10000);
        long traceID = timeStamp * 10000 + randomNumber;
        return Long.toHexString(traceID).toUpperCase();
        
    }
}