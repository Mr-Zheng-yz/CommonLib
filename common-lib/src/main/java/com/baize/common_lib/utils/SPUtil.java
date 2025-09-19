package com.baize.common_lib.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SP工具
 */
public class SPUtil {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    // 初始化方法
    public static void init(Context context, String name) {
        sharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // 存储String类型数据
    public static void putString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // 获取String类型数据
    public static String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // 存储int类型数据
    public static void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // 获取int类型数据
    public static int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // 存储boolean类型数据
    public static void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 获取boolean类型数据
    public static boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // 删除某个key
    public static void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // 清除所有数据
    public static void clear() {
        editor.clear();
        editor.apply();
    }
}