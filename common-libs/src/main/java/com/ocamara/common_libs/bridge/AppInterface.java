package com.ocamara.common_libs.bridge;

import android.app.Application;

/**
 * 主工程模块暴露公共接口
 */
public interface AppInterface {
    Application getApplication();

    //获取服务器时间
    long getServerTimeL();
}
