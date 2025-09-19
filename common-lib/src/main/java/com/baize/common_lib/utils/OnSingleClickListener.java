package com.baize.common_lib.utils;

import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {
    private final long minClickInterval; // 最小点击间隔
    private long mLastClickTime;

    public OnSingleClickListener() {
        this(1000); // 默认1秒
    }

    public OnSingleClickListener(long minInterval) {
        this.minClickInterval = minInterval;
    }

    public abstract void onSingleClick(View v);

    @Override
    public final void onClick(View v) {
        long currentClickTime = System.currentTimeMillis();
        long elapsedTime = currentClickTime - mLastClickTime;
        
        if (elapsedTime <= minClickInterval) {
            return;
        }
        
        mLastClickTime = currentClickTime;
        onSingleClick(v);
    }
}