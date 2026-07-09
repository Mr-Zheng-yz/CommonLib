package com.ocamara.common_libs.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 延时触发的点击时间
 */
public abstract class DelayIntervalTapClickListener implements View.OnTouchListener {
    private static final String TAG = "DelayTapClickListener";
    private long DOWN_UP_INTERVAL = 70;    //down up触发间隔
    private long LAST_CLICK_INTERVAL = 1000;  //距离上次点击触发
    private long lastClickTimeDown;
    private long lastClickTime;
    private String downLocation;

    public DelayIntervalTapClickListener() {
    }

    public DelayIntervalTapClickListener(long down_up_interval, long last_click_interval) {
        this.DOWN_UP_INTERVAL = down_up_interval;
        this.LAST_CLICK_INTERVAL = last_click_interval;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastClickTimeDown = System.currentTimeMillis();
            Log.d(TAG, "按下:" + event.getRawX() + " " + event.getRawY());
            downLocation = event.getRawX() + " " + event.getRawY();
            return true;
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            long now = System.currentTimeMillis();
            long downUpInterval = now - lastClickTimeDown;
            long lastTapInterval = now - lastClickTime;
            Log.d(TAG, "抬起:" + " 间隔:" + downUpInterval);
            if (downUpInterval >= DOWN_UP_INTERVAL && lastTapInterval >= LAST_CLICK_INTERVAL) {
                LogUtil.e(TAG, "-点击抬起时间间隔:" + downUpInterval + " 触发点击，位置：" + event.getRawX() + " " + event.getRawY() + " 按下：" + downLocation);
                onClick(v);
                v.performClick();
                lastClickTime = now;
            } else {
                LogUtil.e(TAG, "-点击抬起时间间隔:" + downUpInterval + " 太短不触发，位置:" + event.getRawX() + " " + event.getRawY() + " 按下：" + downLocation + "  距离上次点击事件触发:" + lastTapInterval);
            }
            return true;
        }
        return false;
    }

    protected abstract void onClick(View view);
}
