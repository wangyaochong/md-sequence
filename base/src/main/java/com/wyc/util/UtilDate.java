package com.wyc.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Slf4j
public class UtilDate {
    public static String getISODatePart(String date) {
        byte var2 = 0;
        byte var3 = 10;
        return date.substring(var2, var3);
    }

    public static String getDatePart(String date) {
        byte var2 = 0;
        byte var3 = 8;
        return date.substring(var2, var3);
    }

    public static String simpleFormatDate(Date date) {
        return (new SimpleDateFormat("yyyy-MM-dd")).format(date);
    }

    public static String getISONowYyyyMMddHHmm00Str() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:00");
    }

    public static String getNowYyyyMMddStr() {
        return DateFormatUtils.format(new Date(), "yyyyMMdd");
    }

    public static String getNowISOYyyyMMddHHmmssStr() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    public static String getISOYyyyMMddHHmmssStr(Date date) {
        return DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getNowHHmmssStr() {
        String var0 = getNowYyyyMMddHHmmssStr();
        byte var1 = 8;
        return var0.substring(var1);
    }

    public static String getNowYyyyMMddHHmmssStr() {
        return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
    }

    public static String getYyyyMMddHHmmssStr(Date date) {
        return DateFormatUtils.format(date, "yyyyMMddHHmmss");
    }

    public static String getYyyyMMddHHmmssStr() {
        return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
    }

    public static String getYyyyMMddHHStr() {
        return DateFormatUtils.format(new Date(), "yyyyMMddHH");
    }

    public static String getYyyyMMddStr(@Nullable Date date) {
        return DateFormatUtils.format(date, "yyyyMMdd");
    }

    public static String getNowISOYyyyMMddStr() {
        return DateFormatUtils.format(new Date(), "yyyy-MM-dd");
    }

    public static String getISOYyyyMMddStr(@Nullable Date date) {
        return DateFormatUtils.format(date, "yyyy-MM-dd");
    }

    public static String getYyyyMMddStrByOffset(String date, int dayCount) {
        try {
            byte var3 = 0;
            byte var4 = 8;
            String var10000 = date.substring(var3, var4);
            return getYyyyMMddStr(
                    DateUtils.addDays(DateUtils.parseDate(var10000, new String[]{"yyyyMMdd"}), dayCount));
        } catch (ParseException var5) {
            log.error(var5.getMessage(), (Throwable) var5);
            return null;
        }
    }

    public static Date fromLocalDate(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDate.atStartOfDay(zoneId);
        return Date.from(zdt.toInstant());
    }

    public static int getDateInterval(String startDate, String endDate) {
        Date date1 = null;
        Date date2 = null;
        long betweenDays = 0L;

        try {
            date1 = (new SimpleDateFormat("yyyyMMdd")).parse(startDate);
            date2 = (new SimpleDateFormat("yyyyMMdd")).parse(endDate);
            long beginTime = date1.getTime();
            long endTime = date2.getTime();
            betweenDays = (endTime - beginTime) / (long) 86400000;
        } catch (ParseException var10) {
            log.error(var10.getMessage(), (Throwable) var10);
        }

        return (int) betweenDays;
    }

    public static Date parseSimpleDate(@Nullable String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException var3) {
            log.error(var3.getMessage(), var3);
            throw new RuntimeException("解析日期失败");
        }
    }

    public static Date parseISOYyyyMMddHHmmssStr(@Nullable String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException var3) {
            log.error(var3.getMessage(), var3);
            throw new RuntimeException("解析日期失败");
        }
    }

    public static Date parseYyyyMMddHHmmssStr(@Nullable String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException var3) {
            log.error(var3.getMessage(), var3);
            throw new RuntimeException("解析日期失败");
        }
    }

    public static boolean isThursday(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE", Locale.CHINA);
        String format = dateFormat.format(date);
        log.info("判断是否是周四的字符串={},date={}", format, date);
        return "星期四".equals(format);
    }

    public static boolean isThursday(@Nullable String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat weekDay = new SimpleDateFormat("EEEE");

        try {
            Date parse = dateFormat.parse(dateString);
            String format = weekDay.format(parse);
            return "星期四".equals(format);
        } catch (ParseException var4) {
            log.error(var4.getMessage(), var4);
            throw new RuntimeException("解析日期失败");
        }
    }

    public static final int getSecondCount(@Nullable String startDate, @Nullable String endDate) {
        Date date = parseYyyyMMddHHmmssStr(startDate);
        Date end = parseYyyyMMddHHmmssStr(endDate);
        return (int) ((end.getTime() - date.getTime()) / (long) 1000);
    }

    public static int getStockSecondCount(String dateTime) {
        byte var3 = 0;
        byte var4 = 8;
        String var10000 = dateTime.substring(var3, var4);
        String dateStr = var10000;
        var3 = 8;
        var10000 = dateTime.substring(var3);
        return var10000.compareTo("120000") < 0
                ? getSecondCount(dateStr + "093000", dateTime)
                : getSecondCount(dateStr + "130000", dateTime) + 7200;
    }

    public static List<String> generateQuarterEndDateListFrom(String startYear) {
        return generateQuarterEndDateList(startYear, getCurrentYear());
    }

    private static String getCurrentYear() {
        int year = DateUtil.year(new Date());
        return Integer.toString(year);
    }

    public static List<String> generateQuarterEndDateList(String startYear, String endYear) {
        return generateQuarterEndDateList(Integer.parseInt(startYear), Integer.parseInt(endYear));
    }

    public static List<String> generateQuarterEndDateList(int startYear, int endYear) {
        List<String> list = new ArrayList<>();
        for (int i = startYear; i <= endYear; i++) {
            list.addAll(generateOneYearQuarterEndDateList(Integer.toString(i)));
        }
        return list;
    }

    public static List<String> generateOneYearQuarterEndDateList(String year) {
        List<String> list = new ArrayList<>();
        list.add(year + "0331");
        list.add(year + "0630");
        list.add(year + "0930");
        list.add(year + "1231");
        return list;
    }

    public static String getEndDateOfLastQuarter(String date) {
        String endDateOfQuarter = getEndDateOfQuarterOfDate(date);
        switch (endDateOfQuarter.substring(4)) {
            case "0331":
                return (Integer.parseInt(date.substring(0, 4)) - 1) + "1231";
            case "0630":
                return date.substring(0, 4) + "0331";
            case "0930":
                return date.substring(0, 4) + "0630";
            case "1231":
                return date.substring(0, 4) + "0930";
            default:
                throw new RuntimeException("getLastQuarterEndDate Error");
        }
    }

    public static String getEndDateOfQuarterOfDate(String date) {
        String month = date.substring(4, 6);
        if (month.equals("01") || month.equals("02") || month.equals("03")) {
            return date.substring(0, 4) + "0331";
        }
        if (month.equals("04") || month.equals("05") || month.equals("06")) {
            return date.substring(0, 4) + "0630";
        }
        if (month.equals("07") || month.equals("08") || month.equals("09")) {
            return date.substring(0, 4) + "0930";
        }
        if (month.equals("10") || month.equals("11") || month.equals("12")) {
            return date.substring(0, 4) + "1231";
        }
        throw new RuntimeException("getEndDateOfQuarterOfDate Error");
    }

    public static String getEndDateOfCurrentQuarter() {
        DateTime dateTime = DateUtil.endOfQuarter(new Date());
        return dateTime.toString("yyyyMMdd");
    }

    public static void main(String[] args) {
        String endDateOfCurrentQuarter = getEndDateOfCurrentQuarter();
        System.out.println(endDateOfCurrentQuarter);
    }

    public static String getOneYearEndDateOfQuarter(String date) {
        String endDate = "";
        if (StringUtils.hasText(date)) {
            String[] dateArr = date.split("-");
            if (dateArr.length == 3) {
                int year = Integer.parseInt(date.substring(0, 4));
                int month = Integer.parseInt(date.substring(5, 7));
                int day = Integer.parseInt(date.substring(8, 10));
                if (month == 1 || month == 2 || month == 3) {
                    endDate = year + "-" + "03" + "-" + "31";
                } else if (month == 4 || month == 5 || month == 6) {
                    endDate = year + "-" + "06" + "-" + "30";
                } else if (month == 7 || month == 8 || month == 9) {
                    endDate = year + "-" + "09" + "-" + "30";
                } else if (month == 10 || month == 11 || month == 12) {
                    endDate = year + "-" + "12" + "-" + "31";
                }
            }
        }
        return endDate;
    }

    public static List<String> buildNeedUpdateQuarterDate(String beginYear, List<String> date) {
        List<String> targetQuarterList = UtilDate.generateQuarterEndDateListFrom(beginYear);

        targetQuarterList.removeIf(
                e -> {
                    for (String s : date) {
                        if (UtilDate.getEndDateOfQuarterOfDate(s).equals(e)) {
                            return true;
                        }
                    }
                    return false;
                });
        //    if (!targetQuarterList.contains(UtilDate.getEndDateOfQuarterOfCurrentDate())) {
        //      targetQuarterList.add(UtilDate.getEndDateOfQuarterOfCurrentDate());
        //    }
        //    if (!targetQuarterList.contains(
        //        UtilDate.getLastQuarterEndDate(UtilDate.getEndDateOfQuarterOfCurrentDate()))) {
        //      targetQuarterList.add(
        //          UtilDate.getLastQuarterEndDate(UtilDate.getEndDateOfQuarterOfCurrentDate()));
        //    }
        return targetQuarterList;
    }
}
