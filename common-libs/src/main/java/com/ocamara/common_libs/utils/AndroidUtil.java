package com.ocamara.common_libs.utils;

import android.app.Application;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;

public class AndroidUtil {
    public AndroidUtil() {
    }

    public static String getProcessName() {
        return Build.VERSION.SDK_INT >= 28 ? Application.getProcessName() : getProcessNameLow();
    }

    public static String getProcessNameLow() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/self/cmdline"));
            String processName = reader.readLine().trim();
            reader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}