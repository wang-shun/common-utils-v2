package com.youzan.sz.common.util;

import com.youzan.sz.common.model.TimeRangeBean;
import com.youzan.sz.jutil.j4log.LoggerFactory;
import com.youzan.util.ThreadSafe;

import org.slf4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * Created by zefa on 16/5/30.
 * modified by zhanguo 修改线程安全
 */
public class DateUtils {
    
    private final static Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DateUtils.class);
    
    public static final String DATE_FORMAT_MDYHMS = "MM/dd/yyyy hh:mm:ss.SSSa";
    
    
    private DateUtils() {
        throw new IllegalAccessError("Utility class");
    }
    
    
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_TL = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };
    
    
    /**
     * 获取按月起止时间戳
     * -1 表示 上一月
     * 0 表示 当月
     * 1 表示 下一个月
     */
    public static TimeRangeBean monthRange(int month) {
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        long endTime = calendar.getTimeInMillis();
        
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DATE));
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        long startTime = calendar.getTimeInMillis();
        
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
     */
    public static TimeRangeBean monthRealRange(int month) {
        Calendar calendar = GregorianCalendar.getInstance();
        long startTime = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, month);
        long endTime = calendar.getTimeInMillis();
        TimeRangeBean timeRangeBean = new TimeRangeBean();
        if (month > 0) {
            timeRangeBean.setStartTime(startTime);
            timeRangeBean.setEndTime(endTime);
        }else {
            timeRangeBean.setStartTime(endTime);
            timeRangeBean.setEndTime(startTime);
        }
        return timeRangeBean;
    }
    
    
    /**
     * 获取距离今天N月
     *
     * @param month 相隔月数，0=>当月，-7=>7月前，2=>2月后
     * @return Date
     */
    public static Date addMonth(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }
    
    
    /**
     * 获取距离今天N天
     *
     * @param day 相隔月数，0=>当天，-7=>7天前，2=>2天后
     * @return Date
     */
    public static Date addDay(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }
    
    
    /**
     * 获取距离今天N天 凌晨的时间戳
     *
     * @param apartDay 相隔天数，0=>当天，-7=>7天前，2=>2天后
     * @return int
     */
    public static long timestampAtZero(int apartDay) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, apartDay);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    
    /**
     * 获取距离现在N小时 整点的时间戳
     *
     * @param apartHour 相隔小时，0=>当前，-1=>1小时前，1=>1小时后
     * @return int
     */
    public static long timestampAtHour(int apartHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, apartHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    
    /**
     * 获取距离现在N小时 整点的时间
     *
     * @param apartHour 相隔小时，0=>当前，-1=>1小时前，1=>1小时后
     * @return int
     */
    public static Date dateAtHour(int apartHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, apartHour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }
    
    
    /**
     * 根据yyyyMMdd类型字符串获取当前0点的时间戳
     *
     * @return int
     */
    public static long timestampAtZero(String yyyyMMdd) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        String todayFull = yyyyMMdd + " 00:00:00";
        int time;
        try {
            time = (int) (sdf.parse(todayFull).getTime() / 1000);
        } catch (ParseException e) {
            throw new RuntimeException("传入参数错误，无法格式化成日期");
        }
        
        return time;
    }
    
    
    public static long timestampCurrent() {
        return new Date().getTime();
        
    }
    
    
    public static int timestampInt(long timestamp) {
        return (int) (timestamp / 1000);
    }
    
    
    public static String timeStamp2Date(int seconds, String dateformat) {
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        return format.format(new Date(Long.valueOf(seconds + "000")));
    }
    
    
    /**
     * 按照指定格式 格式化时间
     */
    public static String date2String(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }
    
    
    /**
     * 按照默认的格式 格式化时间
     */
    public static String date2String(Date date) {
        return DATE_FORMAT_TL.get().format(date);
    }
    
    
    /**
     * 将字符串时间转化为指定格式的Date类型
     */
    public static Date string2Date(String date, String format) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.parse(date);
    }
    
    
    /**
     * 将字符串转化为默认格式的Date类型
     */
    public static Date string2Date(String date) throws ParseException {
        return DATE_FORMAT_TL.get().parse(date);
    }
    
    
    /**
     * 将字符串转化为默认格式的Date类型
     * 战国
     */
    @ThreadSafe
    public static Date parse(String date) {
        try {
            return DATE_FORMAT_TL.get().parse(date);
        } catch (ParseException e) {
            LOGGER.warn("parse date error", e);
            return null;
        }
    }
    
    
    @ThreadSafe
    public static Date parse(String date, String format) {
        try {
            if (format != null & format.length() > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                return simpleDateFormat.parse(date);
            }else {
                return parse(date);
            }
        } catch (ParseException e) {
            LOGGER.warn("parse date error", e);
            return null;
        }
    }
    
    
    /**
     * 判断是否为同一天,是返回true,否返回false
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        if (calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }
        return false;
    }
    
    
    /**
     * 获取指定时间上一个月的天日期列表
     */
    public static List<Date> previousMonthDays(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        int month = calendar1.get(Calendar.MONTH);
        calendar1.set(Calendar.MONTH, month - 1);
        
        List<Date> dates = new ArrayList<>();
        for (int i = calendar1.getMinimum(Calendar.DAY_OF_MONTH); i <= calendar1.getMaximum(Calendar.DAY_OF_MONTH); i++) {
            calendar1.set(Calendar.DAY_OF_MONTH, i);
            dates.add(calendar1.getTime());
        }
        return dates;
    }
    
    
    /**
     * 获取当前时间的小时
     */
    public static int hourOfDate(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        return calendar1.get(Calendar.HOUR_OF_DAY);
    }
    
    
    /**
     * 获取当前时间的小时
     */
    public static List<Date> hoursOfDate(Date date) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.MILLISECOND, 0);
        calendar1.set(Calendar.SECOND, 0);
        List<Date> dates = new ArrayList<>();
        for (int i = calendar1.getMinimum(Calendar.HOUR_OF_DAY); i <= calendar1.getMaximum(Calendar.HOUR_OF_DAY); i++) {
            calendar1.set(Calendar.HOUR_OF_DAY, i);
            dates.add(calendar1.getTime());
        }
        return dates;
    }
    
    
    /**
     * 初始化当天 时分秒为 00:00:00
     */
    private static void initCurDataZero(Calendar calendar) {
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
    }
    
    
    /**
     * 获取开始时间和截至时间之间的每一天
     */
    public static List<Date> daysBetweenStartDateAndEndDate(Date startDate, Date endDate) {
        
        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(startDate);
        initCurDataZero(calendarStart);
        
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(endDate);
        initCurDataZero(calendarEnd);
        
        List<Date> dates = new ArrayList<>();
        
        calendarStart.get(Calendar.DAY_OF_YEAR);
        for (; calendarStart.getTimeInMillis() <= calendarEnd.getTimeInMillis(); ) {
            dates.add(calendarStart.getTime());
            calendarStart.add(Calendar.DAY_OF_YEAR, +1);
        }
        return dates;
    }
    
    
    public static String getTodayStr() {
        return date2String(new Date(), "yyMMdd");
    }
    
    
    public static void main(String[] args) throws ParseException {
        //System.out.println(timestampAtHour(-24));
        
        //        List<Date> list = previousMonthDays(new Date());
        //        System.out.println(list);
        //        try {
        //            System.out.println(
        //                daysBetweenStartDateAndEndDate(string2Date("2015-12-21 00:00:00"), string2Date("2016-01-20 12:00:00")));
        //        } catch (ParseException e) {
        //
        //        }
        System.out.print(DateUtils.date2String(new Date()));
        System.out.print(DateUtils.string2Date("2016-10-12 10:12:00").getTime());
        System.out.print(DateUtils.date2String(new Date()));
        
        System.out.print(DateUtils.timestampAtZero(0));
        
    }
    
}
