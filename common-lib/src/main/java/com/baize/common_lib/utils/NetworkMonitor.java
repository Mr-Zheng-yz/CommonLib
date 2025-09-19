package com.baize.common_lib.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 网络状态监听
 */
public class NetworkMonitor {
    private static final String TAG = "Network";
    private volatile static NetworkMonitor sInstance;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private final List<NetworkListener> networkListenerList = Collections.synchronizedList(new ArrayList<>());

    public static void init(Context context) {
        if (sInstance != null) {
            LogUtil.e(TAG, "NetworkMonitor已经初始化!");
            return;
        }
        synchronized (NetworkMonitor.class) {
            if (sInstance == null) {
                sInstance = new NetworkMonitor(context);
            }
        }
    }

    private NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    private void startMonitoring() {
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // 网络可用
                LogUtil.i(getClass().getSimpleName(), "网络可用.");
                synchronized (networkListenerList) {
                    for (NetworkListener listener: networkListenerList) {
                        listener.networkChange(true, "网络可用");
                    }
                }
            }

            @Override
            public void onLost(Network network) {
                // 网络断开
                LogUtil.i(getClass().getSimpleName(), "网络断开.");
                synchronized (networkListenerList) {
                    for (NetworkListener listener: networkListenerList) {
                        listener.networkChange(false, "网络断开");
                    }
                }
            }

            @Override
            public void onUnavailable() {
                // 没有可用网络
                LogUtil.i(getClass().getSimpleName(), "没有可用网络.");
                synchronized (networkListenerList) {
                    for (NetworkListener listener: networkListenerList) {
                        listener.networkChange(false, "没有可用网络");
                    }
                }
            }
        };

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    private void stopMonitoring() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public static void addNetworkListener(NetworkListener networkListener) {
        if (sInstance != null) {
            boolean needStart = sInstance.networkListenerList.isEmpty();
            sInstance.networkListenerList.add(networkListener);
            if (needStart) {
                sInstance.startMonitoring();
            }
        }
    }

    public static void removeNetworkListener(NetworkListener networkListener) {
        if (sInstance != null) {
            sInstance.networkListenerList.remove(networkListener);
            if (sInstance.networkListenerList.isEmpty()) {
                sInstance.stopMonitoring();
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return true;
    }

    /**
     * 网络变化回调
     */
    public interface NetworkListener {
        void networkChange(boolean available, String message);
    }
}