package com.baize.common_lib.utils;


import com.baize.common_lib.bridge.AppInterface;
import com.baize.common_lib.bridge.BridgeCore;

public class TimeUtil {
    public static long getServerTimeL() {
        AppInterface appImpl = BridgeCore.getInstance().getAppInterface();
        return appImpl != null ? appImpl.getServerTimeL() : System.currentTimeMillis();
    }
}
