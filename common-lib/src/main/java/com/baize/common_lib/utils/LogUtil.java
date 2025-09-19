package com.baize.common_lib.utils;

import android.util.Log;

import com.ocamara.common_libs.BuildConfig;

/**
 * 日志打印统一使用此工具类
 * TODO 統一項目中日志打印
 */
public class LogUtil {
    private static boolean isShowLog = BuildConfig.DEBUG;
    public static boolean isShowErrLog = true;

    public static void toggleLogEnable() {
        isShowLog = !isShowLog;
        if (isShowLog) {
            LogUtil.i("------日志打印已经开启!");
        } else {
            Log.i(TAG, "------日志打印已经关闭!");
        }
    }

    public static final String TAG = "CommonLogUtil";

    public static void d(String msg) {
        i(null, msg);
    }

    public static void d(String suffix, String msg) {
        if (isShowLog) {
            Log.i(TAG + (suffix == null ? "" : "_" + suffix), msg);
        }
    }

    public static void i(String msg) {
        i(null, msg);
    }

    public static void i(String suffix, String msg) {
        if (isShowLog) {
            Log.i(TAG + (suffix == null ? "" : "_" + suffix), msg);
        }
    }

    public static void w(String msg) {
        i(null, msg);
    }

    public static void w(String suffix, String msg) {
        if (isShowLog) {
            Log.i(TAG + (suffix == null ? "" : "_" + suffix), msg);
        }
    }

    public static void e(String msg) {
        e(null, msg);
    }

    public static void e(String suffix, String msg) {
        String tag = TAG + (suffix == null ? "" : "_" + suffix);
        if(isShowLog || isShowErrLog){
            Log.e(tag, msg);
        }
        try {
            FileLogger.write(tag + "： —— "+msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void wi(String msg) {
        wi(null, msg);
    }

    public static void wi(String suffix, String msg) {
        String tag = TAG + (suffix == null ? "" : "_" + suffix);
        if (isShowLog || isShowErrLog) {
            Log.i(tag, msg);
        }
        try {
            FileLogger.write(tag + "： —— " + msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
