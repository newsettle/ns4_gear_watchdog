package com.creditease.ns4.gear.watchdog.common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author outman
 * @description 时间工具类
 * @date 2019/1/15
 */
public class DateUtil {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 正常的日期格式
     */
    public static final String DATE_PATTERN_FULL = "MMddHHmmss.SSS";
    /**
     * 不带符号的日期格式，用来记录时间戳
     */
    public static final String DATE_PATTERN_NOMARK = "yyyy-MM-dd-HH";
    /**
     * 不带符号的日期格式，用来记录时间戳
     */
    public static final String DATE_PATTERN_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * 时间格式化
     *
     * @param time 待转换的时间
     * @return 转换之后的时间传
     */
    public static String timeToStr(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    /**
     * 时间格式化
     *
     * @param time 待转换的时间
     * @return 转换之后的时间传
     */
    public static String timeToStr(long time,String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    /**
     * 获取当前日期
     * @return yyyy-MM-dd
     */
    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(new Date());
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentDate(String pattern) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentDate(Date date,String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
}
