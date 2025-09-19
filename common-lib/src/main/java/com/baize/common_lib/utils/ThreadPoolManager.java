package com.baize.common_lib.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理类
 */
public class ThreadPoolManager {
    private static volatile ThreadPoolManager sInstance;

    public static ThreadPoolManager getInstance() {
        if (sInstance == null) {
            synchronized (ThreadPoolManager.class) {
                if (sInstance == null) {
                    sInstance = new ThreadPoolManager();
                }
            }
        }
        return sInstance;
    }

    // 获取 CPU 核心数
    private int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // I/O 密集型线程池
    private ExecutorService ioExecutor = new ThreadPoolExecutor(
            CPU_COUNT * 2,       // 核心线程数
            CPU_COUNT * 2 + 1,   // 最大线程数
            60L,                 // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue()
    );

    // CPU 密集型线程池
    private ExecutorService cpuExecutor = new ThreadPoolExecutor(
            CPU_COUNT + 1,       // 核心线程数
            CPU_COUNT + 1,       // 最大线程数
            60L,                 // 空闲线程存活时间
            TimeUnit.SECONDS,
            new LinkedBlockingQueue()
    );

    // 提交 I/O 密集型任务
    public void executeIoTask(Runnable runnable) {
        ioExecutor.execute(runnable);
    }

    // 提交 CPU 密集型任务
    public void executeCpuTask(Runnable runnable) {
        cpuExecutor.execute(runnable);
    }

    // 关闭所有线程池
    public void shutdownAll() {
        ioExecutor.shutdown();
        cpuExecutor.shutdown();
    }

}