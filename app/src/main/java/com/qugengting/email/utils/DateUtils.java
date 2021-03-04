package com.qugengting.email.utils;

import android.content.Context;

import com.qugengting.email.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by xuruibin on 2017/11/21
 * 日期转化工具类
 */

public class DateUtils {

    private static Context mContext;

    public static void initContext(Context context) {
        mContext = context;
    }

    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    public static long stringToLong(String strTime) {
        Date date = null; // String类型转成date类型
        try {
            date = stringToDate(strTime, "yyyy-MM-dd'T'HH:mm:ss.SSS");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return 0;
        } else {
            long currentTime = date.getTime(); // date类型转成long类型
            return currentTime;
        }
    }

    public static long stringToLong(String strTime, String formatType) {
        Date date = null; // String类型转成date类型
        try {
            date = stringToDate(strTime, formatType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date == null) {
            return 0;
        } else {
            long currentTime = date.getTime(); // date类型转成long类型
            return currentTime;
        }
    }

    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    public static String longToString(long currentTime, String formatType) {
        Date date = null; // long类型转成Date类型
        try {
            date = longToDate(currentTime, formatType);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateToString(date, formatType);
    }

    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType, Locale.getDefault()).format(data);
    }

    /**
     * @param oldTime 较小的时间
     * @return -1 ：同一天.    0：昨天 .   1 :一周内. 一周以前2
     * @author LuoB.
     */
    private static int isYeaterday(Date oldTime) throws ParseException {
        Date newTime = new Date();
        //将下面的 理解成  yyyy-MM-dd 00：00：00 更好理解点
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = format.format(newTime);
        //今天0时
        Date today = format.parse(todayStr);
        //昨天 86400000=24*60*60*1000 一天
        if ((today.getTime() - oldTime.getTime()) > 0 && (today.getTime() - oldTime.getTime()) <= 86400000) {
            return 0;
        } else if ((today.getTime() - oldTime.getTime()) > 86400000 && (today.getTime() - oldTime.getTime()) <= 86400000 * 8) {
            return 1;
        } else if ((today.getTime() - oldTime.getTime()) <= 0) { //至少是今天
            return -1;
        } else { //至少是一周前
            return 2;
        }

    }

    /**
     * 统一时间显示
     */
    public static String getFormatDate(Date trialTime) {
        SimpleDateFormat sdf;
        int yeaterday = 2;
        try {
            yeaterday = isYeaterday(trialTime);
        } catch (ParseException e) {
            yeaterday = 2;
        }
        //今天的消息，不带日期
        if (yeaterday == -1) {
            int time = (int) ((System.currentTimeMillis() - trialTime.getTime()) / 1000);
            if (time >= 0 && time <= 60) {
                //刚刚
                return mContext.getString(R.string.common_just_now);
            } else if (time > 60 && time <= 300) {
                //5分钟之内
                return Math.max(time / 60, 1) + mContext.getString(R.string.common_minutes_ago);
            } else {
                //今天其他时间
                sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return sdf.format(trialTime);
            }
        }
        //昨天的消息
        if (yeaterday == 0) {
            sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return mContext.getString(R.string.common_yesterday) + sdf.format(trialTime);
        }
        //一周内的消息
        if (yeaterday == 1) {
            sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Calendar trial = Calendar.getInstance();
            trial.setTime(trialTime);
            return mContext.getString(R.string.common_week) + getWeek(trial.get(Calendar.DAY_OF_WEEK)) + " " + sdf.format(trialTime);
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(trialTime);
    }

    private static String getWeek(int week) {
        if (week == 1) {
            return "日";
        } else if (week == 2) {
            return "一";
        } else if (week == 3) {
            return "二";
        } else if (week == 4) {
            return "三";
        } else if (week == 5) {
            return "四";
        } else if (week == 6) {
            return "五";
        } else if (week == 7) {
            return "六";
        }
        return "";
    }

    /**
     * 判断当前日期是星期几
     */
    public static String dayForWeek(long pTime) {
        Calendar trial = Calendar.getInstance();
        try {
            Date date = new Date(pTime);
            trial.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mContext.getString(R.string.common_week) + getWeek(trial.get(Calendar.DAY_OF_WEEK));
    }
}
