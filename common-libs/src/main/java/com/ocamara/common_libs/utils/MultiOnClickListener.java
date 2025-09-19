package com.ocamara.common_libs.utils;

import android.os.Handler;
import android.view.View;

public abstract class MultiOnClickListener implements View.OnClickListener {
    private static final int MAX_CLICKS = 5;
    private static final long TIME_WINDOW = 6000L;
    private int clickCount = 0;
    private long startTime = 0L;
    private Handler handler = new Handler();

    @Override
    public void onClick(View v) {
        if (this.clickCount == 0) {
            this.startTime = System.currentTimeMillis();
        }

        ++this.clickCount;
        if (this.clickCount >= MAX_CLICKS) {
            long elapsedTime = System.currentTimeMillis() - this.startTime;
            if (elapsedTime <= TIME_WINDOW) {
                this.onMultiClick(v);
            }
            this.resetClickTracking();
        } else {
            this.handler.removeCallbacksAndMessages(null);
            this.handler.postDelayed(this::resetClickTracking, TIME_WINDOW);
        }

    }

    private void resetClickTracking() {
        this.clickCount = 0;
        this.startTime = 0L;
    }

    public abstract void onMultiClick(View var1);
}
