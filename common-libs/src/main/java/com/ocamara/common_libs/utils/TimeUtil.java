package com.ocamara.common_libs.utils;

import com.ocamara.common_libs.bridge.AppInterface;
import com.ocamara.common_libs.bridge.BridgeCore;

public class TimeUtil {
    public static long getServerTimeL() {
        AppInterface appImpl = BridgeCore.getInstance().getAppInterface();
        return appImpl != null ? appImpl.getServerTimeL() : System.currentTimeMillis();
    }
}
