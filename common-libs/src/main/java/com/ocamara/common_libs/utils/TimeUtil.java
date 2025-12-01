package com.ocamara.common_libs.utils;

import android.util.Log;

import com.ocamara.common_libs.bridge.AppInterface;
import com.ocamara.common_libs.bridge.BridgeCore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {
    public static long getServerTimeL() {
        AppInterface appImpl = BridgeCore.getInstance().getAppInterface();
        return appImpl != null ? appImpl.getServerTimeL() : System.currentTimeMillis();
    }

    /**
     * 时间戳转指定格式时间字符串
     * @param timestamp 时间戳（毫秒）
     * @param pattern 时间格式，如 "yyyyMMdd-HHmmss"
     * @return 格式化后的时间字符串
     */
    public static String timestampToString(long timestamp, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault()); // 设置为系统默认时区
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 格式时间字符串转时间戳
     * @param timeString 时间字符串
     * @param pattern 时间格式，必须与字符串格式一致
     * @return 时间戳（毫秒），解析失败返回-1
     */
    public static long stringToTimestamp(String timeString, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            Date date = sdf.parse(timeString);
            return date != null ? date.getTime() : -1;
        } catch (ParseException e) {
            Log.e("", "!!stringToTimestamp err:" + e.getMessage());
            return -1;
        }
    }
}
