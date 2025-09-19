package com.baize.common_lib.utils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 日志写入工具
 */
public class FileLogger {
    private static final String TAG = "FileLogger";
    private static final SimpleDateFormat logSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String LOG_DIR = "logs"; // 日志文件夹
    private static final long MAX_LOG_SIZE = 500 * 1024; // 单个日志文件最大500K
    private static final int MAX_LOG_DAYS = 7; // 日志保留最长7天
    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private BufferedWriter writer = null;
    private File currentLogFile = null;

    private static volatile FileLogger sInstance;

    public static FileLogger getInstance() {
        if (sInstance == null) {
            synchronized (FileLogger.class) {
                if (sInstance == null) {
                    sInstance = new FileLogger();
                }
            }
        }
        return sInstance;
    }

    private FileLogger() {}

    public void init(Context context) {
        cleanOldLogs(context); // 清理旧日志
        startWriteThread(context); // 启动写入线程
    }

    private void cleanOldLogs(Context context) {
        File logDir = ensureLogDir(context);
        if (!logDir.exists()) return;
        long cutoffTime = getCurrentTimeMillis() - MAX_LOG_DAYS * 24 * 3600 * 1000L;
        Log.d(TAG, "--旧日志截止时间:" + cutoffTime);
        File[] files = logDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() < cutoffTime) {
                    boolean result = file.delete();
                    Log.d(TAG, "--删除旧日志文件:" + file.getName() + " 结果:" + result);
                }
            }
        }
    }

    private void startWriteThread(Context context) {
        HandlerThread handlerThread = new HandlerThread("FileLoggerThread");
        handlerThread.start();
        Handler handler = new Handler(handlerThread.getLooper());
        handler.post(() -> {
            while (true) {
                try {
                    String logMsg = logQueue.take(); // 阻塞直到有日志
//                    Log.d(TAG, "++写入日志：" + logMsg);
                    checkAndRotateFile(context); // 检查是否需要切换文件
                    if (writer != null) {
                        writer.write(logMsg);
                        writer.flush(); // 按需刷新缓冲
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "--写入日志错误:" + e.getMessage());
                }
            }
        });
    }

    private void checkAndRotateFile(Context context) throws IOException {
        File logDir = ensureLogDir(context);
        if (currentLogFile == null || currentLogFile.length() > MAX_LOG_SIZE) {
            if (writer != null) {
                writer.close();
            }
            String date = new SimpleDateFormat("yyyyMMdd-HHmmss").format(getCurrentTimeMillis());
            currentLogFile = new File(logDir, "log_" + date + ".txt");
            Log.d(TAG, "++++创建日志文件：" + currentLogFile.getName());
            writer = new BufferedWriter(new FileWriter(currentLogFile, true), 8192); // 8KB缓冲
        }
    }

    public static void write(String message) {
        getInstance().log(message);
    }

    // 写入日志
    public void log(String message) {
        String timestamp = "[ " + logSDF.format(getCurrentTimeMillis()) +" ]";
        String logMsg = timestamp +  " - " + message + "\n";
        logQueue.offer(logMsg); // 非阻塞写入队列
    }

    private long getCurrentTimeMillis() {
        return TimeUtil.getServerTimeL();
    }

    public File ensureLogDir(Context context) {
        File logDir = new File(context.getExternalFilesDir(null), LOG_DIR);
        logDir.mkdirs();
        return logDir;
    }

    //获取闪退日志存储目录
    public String getLogDir(Context context) {
        return ensureLogDir(context).getPath();
    }
}
