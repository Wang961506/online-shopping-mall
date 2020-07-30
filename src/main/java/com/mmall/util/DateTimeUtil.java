package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * @author ZheWang
 * @create 2020-07-19 16:53
 */
public class DateTimeUtil {
    //joda-time实现
    private static final String STANDARD_FORMAT="yy-MM-dd HH:mm:ss";
    public static Date strToDate(String datetimestr, String formatstr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatstr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(datetimestr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date, String formatstr) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatstr);
    }
    public static Date strToDate(String datetimestr) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(datetimestr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }
}
