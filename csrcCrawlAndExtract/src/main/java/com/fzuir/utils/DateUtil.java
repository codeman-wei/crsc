package com.fzuir.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date checkDate = null;
        try {
            checkDate = df.parse(date);
        } catch (ParseException e) {
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
}
