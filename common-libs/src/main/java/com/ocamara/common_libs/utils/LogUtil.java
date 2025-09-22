package com.ocamara.common_libs.utils;

import android.util.Log;

/**
 * 日志打印统一使用此工具类
 * TODO 統一項目中日志打印
 */
public class LogUtil {
    private static boolean isShowLog = true;
    public static boolean isShowErrLog = true;  //错误日志
    public static boolean httpLogEnable = false;  //是否写入网络请求日志
    public static boolean isShowDebugLog = false;  //调试日志

    public static void toggleLogEnable() {
        isShowLog = !isShowLog;
        if (isShowLog) {
            LogUtil.i("------日志打印已经开启!");
        } else {
            Log.i(TAG, "------日志打印已经关闭!");
        }
    }

    public static void toggleHttpLogEnable(boolean enable) {
        httpLogEnable = enable;
        if (httpLogEnable) {
            LogUtil.wi("------网络日志开启写入!");
        } else {
            LogUtil.wi("------网络日志已经关闭.");
        }
    }

    public static void toggleDebugLogEnable(boolean enable) {
        isShowDebugLog = enable;
        if (isShowDebugLog) {
            LogUtil.wi("------Debug日志开启写入!");
        } else {
            LogUtil.wi("------Debug日志已经关闭.");
        }
    }

    public static final String TAG = "CommonLog";

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

    public static void di(String suffix, String msg) {
        if (isShowDebugLog) {
            String tag = TAG + (suffix == null ? "" : "_" + suffix);
            Log.i(tag, msg);
            try {
                FileLogger.write(tag + "： —— " + msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void http(String msg) {
        String tag = TAG + "_HTTP";
        if (isShowLog) {
            Log.i(tag, msg);
        }
        if (httpLogEnable) {
            FileLogger.write(tag + "： —— " + msg);
        }
    }
}
