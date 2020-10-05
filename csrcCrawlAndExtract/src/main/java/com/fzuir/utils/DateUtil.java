package com.fzuir.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DateUtil {
    /**
     * 比较一个日期是否在指定的n天前到今天这个范围内，比如当before
     * @param date  待检查日期
     * @param beforeDays  指定多少天以前
     * @return 是否在时间范围内
     */
    public static boolean checkDate(String date, int beforeDays){
        if (beforeDays == -1) {
            return true;
        }

        DateFormat df;
        if (date.indexOf("年") != -1) {
            df = new SimpleDateFormat("yyyy年MM月dd日");
        } else {
            df = new SimpleDateFormat("yyyy-MM-dd");
        }

        Date checkDate;
        try {
            checkDate = df.parse(date);
        } catch (ParseException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -beforeDays - 1);
        Date before =  calendar.getTime();
        if (checkDate.before(today) && checkDate.after(before)) {
            return true;
        } else {
            return false;
        }

    }


    /**
     * 统一日期格式为YYYY-mm-dd
     * @param date
     * @return
     */
    public static String cleanDate(String date) {
        String regex = "[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        if (matcher.find()) {
            date = matcher.group(0).replaceAll("[^0-9]","-");
            return date;
        } else {
            return "";
        }
    }
}
