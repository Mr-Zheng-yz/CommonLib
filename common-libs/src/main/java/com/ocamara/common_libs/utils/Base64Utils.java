package com.ocamara.common_libs.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;

public class Base64Utils {
    public static String encode(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String encode(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    public static String decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public static byte[] decodeToBytes(String base64, boolean urlSafe) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        return Base64.decode(base64, urlSafe ? Base64.URL_SAFE : Base64.DEFAULT);
    }

    public static String encodeUrlSafe(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    public static String encodeUrlSafe(byte[] input) {
        if (input == null || input.length == 0) {
            return null;
        }
        return Base64.encodeToString(input, Base64.URL_SAFE | Base64.NO_WRAP);
    }
    public static String decodeUrlSafe(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        byte[] decodedBytes = Base64.decode(base64, Base64.URL_SAFE | Base64.NO_WRAP);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}