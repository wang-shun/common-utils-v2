package com.youzan.sz.common.util;

import com.youzan.sz.common.model.TimeRangeBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by zefa on 16/5/30.
 */
public class DateUtils {
    /**
     * 获取按月起止时间戳
     *  -1 表示 上一月
     *  0 表示 当月
     *  1 表示 下一个月
     * @param month
     * @return
     */
    public static TimeRangeBean monthRange(int month) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        long endTime = calendar.getTimeInMillis()/1000;

        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        long startTime = calendar.getTimeInMillis()/1000;

        TimeRangeBean timeRangeBean = new TimeRangeBean();
        timeRangeBean.setStartTime(startTime);
        timeRangeBean.setEndTime(endTime);
        return timeRangeBean;
    }

    /**
     * 获取从当前时间到跨起止时间戳
     * -1 表示 上一月
     * 0 表示 当月
     * 1 表示 下一个月
     * @param month
     * @return
     */
    public static TimeRangeBean monthRealRange(int month) {
        Calendar calendar = GregorianCalendar.getInstance();
        long startTime = calendar.getTimeInMillis()/1000;
        calendar.add(Calendar.MONTH, month);
        long endTime = calendar.getTimeInMillis()/1000;
        TimeRangeBean timeRangeBean = new TimeRangeBean();
        if(month > 0) {
            timeRangeBean.setStartTime(startTime);
            timeRangeBean.setEndTime(endTime);
        }else{
            timeRangeBean.setStartTime(endTime);
            timeRangeBean.setEndTime(startTime);
        }
        return timeRangeBean;
    }

    /**
     * 获取距离今天N天 凌晨的时间戳
     * @param apartDay 相隔天数，0=>当天，-7=>7天前，2=>2天后
     * @return int
     */
    public static int timestampAtZero(int apartDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, apartDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return (int)(calendar.getTimeInMillis() / 1000);
    }

    /**
     * 获取距离现在N小时 整点的时间戳
     * @param apartHour 相隔小时，0=>当前，-1=>1小时前，1=>1小时后
     * @return int
     */
    public static int timestampAtHour(int apartHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, apartHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return (int)(calendar.getTimeInMillis() / 1000);
    }

    /**
     * 根据yyyyMMdd类型字符串获取当前0点的时间戳
     * @param yyyyMMdd
     * @return int
     */
    public static int timestampAtZero(String yyyyMMdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String todayFull = yyyyMMdd + " 00:00:00";
        int time;
        try {
            time =(int)(sdf.parse(todayFull).getTime()/1000);
        } catch (ParseException e) {
            throw new RuntimeException("传入参数错误，无法格式化成日期");
        }

        return time;
    }

    public static int timestampCurrent(){
        return (int)((new Date()).getTime()/1000);

    }

    public static int timestampInt(long timestamp){
        return (int)(timestamp/1000);
    }

    public static String timeStamp2Date(int seconds, String dateformat)
    {
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        return format.format(new Date(Long.valueOf(seconds + "000")));
    }

    public static void main(String[] args) {
        System.out.println(timestampAtHour(0));
    }
}
