package com.ocamara.common_libs.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 延时触发的点击时间
 */
public abstract class DelayTapClickListener implements View.OnTouchListener {
    private static final String TAG = "DelayTapClickListener";
    private static final long INTERVAL_TIME = 70;
    private long lastClickTimeDown;
    private String downLocation;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastClickTimeDown = System.currentTimeMillis();
            Log.d(TAG, "按下:" + event.getRawX() + " " + event.getRawY());
            downLocation = event.getRawX() + " " + event.getRawY();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            long interval = System.currentTimeMillis() - lastClickTimeDown;
            Log.d(TAG, "抬起:" + " 间隔:" + interval);
            if (interval >= INTERVAL_TIME) {
                LogUtil.e(TAG, "-点击抬起时间间隔:" + interval + " 触发点击，位置：" + event.getRawX() + " " + event.getRawY() + " 按下：" + downLocation);
                onClick(v);
            } else {
                LogUtil.e(TAG, "-点击抬起时间间隔:" + interval + " 太短不触发，位置:" + event.getRawX() + " " + event.getRawY() + " 按下：" + downLocation);
            }
            return true;
        }
        return false;
    }

    protected abstract void onClick(View view);
}
