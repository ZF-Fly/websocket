package com.zf.microservice.wsxch.restapi.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zf.microservice.wsxch.restapi.object.constant.GlobalConst;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * 日期类型工具类
 */
public class DateUtil {

    /**
     * 获取每分钟的时间戳
     *
     * @return
     */
    public static Long getPerMinuteTimestamp() {
        LocalDate localDate = LocalDate.now();
        LocalTime localTime = LocalTime.now();
        return LocalDateTime.of(localDate.getYear(), localDate.getMonth(), localDate.getDayOfMonth(), localTime.getHour(), localTime.getMinute(), 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 获取n天之前的凌晨起始时间(Date)
     *
     * @param n
     * @return
     */
    public static Date getLastDaysDate(int n) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - n);

        return calendar.getTime();
    }


    /**
     * 返回自格林尼治标准时间1970年1月1日00:00:00以来的毫秒数。
     *
     * @return 返回自格林尼治标准时间1970年1月1日00:00:00以来的毫秒数，表示当前时间。
     */
    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前系统时间的日期对象
     *
     * @return 当前系统时间日期对象
     */
    public static Calendar getCurrentCalendar() {
        return dateToCalendar(timestampToDate(getCurrentTimestamp()));
    }

    /**
     * 获取当前系统时间
     *
     * @return 当前系统时间 yyyy-MM-dd 00:00:00
     */
    public static Date getCurrentDate() {
        Calendar calendar = getCurrentCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 获取当前系统时间所在的年份
     *
     * @return 当前年份
     */
    public static Integer getCurrentYear() {
        return getCurrentCalendar().get(Calendar.YEAR);
    }

    /**
     * 指定年月日创建日期
     *
     * @param year  指定年份
     * @param month 参考Calendar.MONTH值定义
     * @param day   指定天
     * @return 返回指定日
     */
    public static Date createDate(Integer year, Integer month, Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * 指定月份的第几个星期几创建日期
     *
     * @param year    指定年份
     * @param month   参考Calendar.MONTH值定义
     * @param weekday 参考Calendar.DAY_OF_WEEK值定义
     * @param count   > 0：第几个星期几；-1：最后一个星期几
     * @return 满足条件的日期
     */
    public static Date createDate(Integer year, Integer month, Integer weekday, Integer count) {
        Date sourceDate = createDate(year, month, 1);
        Date targetDate = sourceDate;

        int currentWeekday = getWeekday(sourceDate);
        if (currentWeekday == weekday) {
            --count;
        }

        while (true) {
            currentWeekday = getWeekday(sourceDate);
            if (currentWeekday < weekday) {
                sourceDate = addDays(sourceDate, weekday - currentWeekday);
            } else {
                sourceDate = addDays(sourceDate, weekday + 7 - currentWeekday);
            }

            --count;
            Integer currentMonth = dateToCalendar(sourceDate).get(Calendar.MONTH);

            if (count == -1) {
                break;
            }
            if (!currentMonth.equals(month)) {
                break;
            }

            targetDate = sourceDate;
        }

        return targetDate;
    }

    /**
     * 判断指定日期是否为周末
     *
     * @param date 指定日期
     * @return false - 不是周末， true - 是周末
     */
    public static Boolean isWeekend(Date date) {
        Calendar calendar = dateToCalendar(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * 获取星期天数
     * <description>返回指定日期在星期中的天数。参考Calendar.DAYOF_WEEK返回值定义</description>
     *
     * @param date 指定日期
     * @return 指定日期的星期天数
     */
    public static Integer getWeekday(Date date) {
        return dateToCalendar(date).get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 获取星期显示名称
     *
     * @param date 指定日期
     * @return 获取星期显示名称
     */
    public static String getWeekdayDisplay(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE");

        return format.format(date);
    }

    /**
     * 计算两个日期的间隔天数
     *
     * @param begin 起始日期
     * @param end   结尾日期
     * @return 两个日期之间间隔天数
     */
    public static Integer diffDays(Date begin, Date end) {
        long diffTicks = end.getTime() - begin.getTime();

        return BigDecimalUtil.div(
                diffTicks,
                MILLI_SECONDS_IN_DAY,
                0,
                BigDecimal.ROUND_DOWN
        ).intValue();
    }

    /**
     * 获取指定天数差的工作日
     *
     * @param begin      起始日期
     * @param diffDay    指定天数
     * @param holidaySet 假期日集合
     * @return 指定天数差的工作日日期
     */
    public static Date getTargetBusinessDay(Date begin, Integer diffDay, Set<String> holidaySet) {
        Date targetDate = null;
        int diffBusinessDays = 0, realDiffDay = diffDay;

        if (diffDay < 0) {
            while (diffBusinessDays < Math.abs(diffDay)) {
                targetDate = addDays(begin, realDiffDay);
                diffBusinessDays = diffBusinessDays(targetDate, begin, holidaySet);
                realDiffDay -= (Math.abs(diffDay) - diffBusinessDays);
            }


        } else {
            while (diffBusinessDays < diffDay) {
                targetDate = addDays(begin, realDiffDay);
                diffBusinessDays = diffBusinessDays(begin, targetDate, holidaySet);
                realDiffDay += (diffDay - diffBusinessDays);
            }
        }

        if (diffBusinessDays != Math.abs(diffDay)) {
            System.err.println("math wrong");
            targetDate = null;
        }

        return targetDate;
    }

    /**
     * 计算两个日期间隔工作日天数
     *
     * @param begin      第一个日期
     * @param end        第二个日期
     * @param holidaySet 假期日集合
     * @return 两个日期之前存在的工作日天数
     */
    public static int diffBusinessDays(Date begin, Date end, Set<String> holidaySet) {
        int diffBusinessDays = 0;
        boolean isBeginLessThanEnd = true;

        int diffDays = diffDays(begin, end);

        if (diffDays < 0) {
            diffDays = Math.abs(diffDays);
            isBeginLessThanEnd = false;
        }

        for (int i = 0; i < diffDays; ++i) {
            Date date;
            if (isBeginLessThanEnd) {
                date = addDays(begin, i);
            } else {
                date = addDays(begin, -i);
            }

            if (isWeekend(date) || holidaySet.contains(dateToStr(date))) {
                continue;
            }

            if (isBeginLessThanEnd) {
                ++diffBusinessDays;
            } else {
                --diffBusinessDays;
            }
        }

        return diffBusinessDays;
    }

    /**
     * 调整指定日期
     *
     * @param type        调整类型,参考Calendar常量定义
     * @param initialDate 指定日期
     * @param value       调整值
     * @return 调整之后的日期
     */
    public static Date add(int type, Date initialDate, int value) {
        Calendar calendar = dateToCalendar(initialDate);
        calendar.add(type, value);

        return calendar.getTime();
    }

    /**
     * 指定日期添加年
     *
     * @param initialDate 初始的日期
     * @param years       日期年数
     * @return 获得日期
     */
    public static Date addYears(Date initialDate, int years) {
        return add(Calendar.YEAR, initialDate, years);
    }

    /**
     * 指定日期添加天数
     *
     * @param initialDate 初始的日期
     * @param days        日期天数
     * @return 获得日期
     */
    public static Date addDays(Date initialDate, int days) {
        return add(Calendar.DATE, initialDate, days);
    }

    /**
     * 指定时间添加小时
     *
     * @param initialDate 初始的日期
     * @param hours       日期小时数
     * @return 获得日期
     */
    public static Date addHours(Date initialDate, int hours) {
        return add(Calendar.HOUR, initialDate, hours);
    }

    /**
     * 指定时间添加分钟
     *
     * @param initialDate 初始的日期
     * @param minutes     日期分钟数
     * @return 获得日期
     */
    public static Date addMinutes(Date initialDate, int minutes) {
        return add(Calendar.MINUTE, initialDate, minutes);
    }

    /**
     * 字符串转换为日期，默认格式 yyyy-MM-dd
     *
     * @param dateStr 原始日期（字符串）
     * @return 指定日期（日期类型）
     */
    public static Date strToDate(String dateStr) throws ParseException {
        return strToDate(dateStr, DEFAULT_DATE_PATTERN);
    }

    /**
     * 字符串转换为日期，指定格式
     *
     * @param dateStr 原始日期（字符串）
     * @param pattern 日期转换格式
     * @return 指定日期（日期类型）
     */
    public static Date strToDate(String dateStr, String pattern) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.parse(dateStr);
    }

    /**
     * 日期装换为字符串，默认格式 yyyy-MM-dd
     *
     * @param date 原始日期（日期类型）
     * @return 指定日期（字符串）
     */
    public static String dateToStr(Date date) {
        return dateToStr(date, DEFAULT_DATE_PATTERN);
    }

    /**
     * 字符串转换为日期，指定格式
     *
     * @param date    原始日期（日期类型）
     * @param pattern 日期转换格式
     * @return 指定日期（字符串）
     */
    public static String dateToStr(Date date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.format(date);
    }

    /**
     * Long转换为Date
     *
     * @param timestamp 指定时间戳
     * @return 指定日期
     */
    public static Date timestampToDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        return calendar.getTime();
    }

    /**
     * Date转换为Long
     *
     * @param date 指定日期（日期格式）
     * @return 指定日期（时间戳格式）
     */
    public static Long dateToTimestamp(Date date) {
        return date.getTime();
    }

    /**
     * 字符串转换为时间戳 yyyy-MM-dd
     *
     * @param dateStr 指定日期
     * @return 指定日期的时间戳
     */
    public static Long strToTimeStamp(String dateStr) throws ParseException {
        return strToTimeStamp(dateStr, DEFAULT_DATE_PATTERN);
    }

    /**
     * 字符串转换为时间戳
     *
     * @param dateStr 指定日期
     * @param pattern 日期转换格式
     * @return 指定日期的时间戳
     */
    public static Long strToTimeStamp(String dateStr, String pattern) throws ParseException {
        return strToDate(dateStr, pattern).getTime();
    }

    /**
     * 字符串转换为时间戳 yyyy-MM-dd
     *
     * @param timestamp 指定日期
     * @return 指定日期（字符串）
     */
    public static String timeStampToStr(Long timestamp) {
        return dateToStr(timestampToDate(timestamp));
    }

    /**
     * 字符串转换为时间戳
     *
     * @param timestamp 指定日期
     * @param pattern   日期转换格式
     * @return 指定日期（字符串）
     */
    public static String timeStampToStr(Long timestamp, String pattern) {
        return dateToStr(timestampToDate(timestamp), pattern);
    }

    /**
     * Date转换为Calendar
     *
     * @param date 指定日期
     * @return Calendar对象（指定日期）
     */
    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar;
    }

    public static JSONArray timeStampToStr(Collection<?> dataList, String pattern, String... keys) {
        JSONArray dataArray = JSON.parseArray(JSON.toJSONString(dataList));
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject dataJson = dataArray.getJSONObject(i);
            for (String key : keys) {
                if (dataJson.containsKey(key)) {
                    dataJson.put(key, timeStampToStr(dataJson.getLong(key), pattern));
                }
            }
        }

        return dataArray;
    }

    public static JSONArray timeStampToStr(Collection<?> dataList) {
        return timeStampToStr(dataList, DEFAULT_DATETIME_PATTERN, "createdAt", "updatedAt");
    }

    public static Long getMonthTimestamp(Integer month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static Long getCurrentMonthTimestamp() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * 获取上n天的凌晨起始时间戳
     *
     * @param calendar
     * @param n
     * @return
     */
    public static long getLastDaysTimestamp(Calendar calendar, int n) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - n);
        return calendar.getTimeInMillis();
    }

    public static final long MILLI_SECONDS_IN_DAY = GlobalConst.SECONDS_IN_DAY * 1000;
    public static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";
    public static final String DEFAULT_HOUR_MINUTE_PATTERN = "HH:mm";
    public static final String DEFAULT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

}
