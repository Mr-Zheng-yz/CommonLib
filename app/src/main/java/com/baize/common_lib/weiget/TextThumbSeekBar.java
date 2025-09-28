package com.baize.common_lib.weiget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;

/***
 * 2022.08.08 自定义滑动进度条控件
 */
public class TextThumbSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {
    private int mThumbSize;//绘制滑块宽度
    private TextPaint mTextPaint;//绘制的文本
    private int mSeekBarMin=0;//滑块开始值

    public TextThumbSeekBar(Context context) {
        this(context, null);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public TextThumbSeekBar(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        mThumbSize = 50;
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.parseColor("#2C415E"));
        mTextPaint.setTextSize(24);
        mTextPaint.setTypeface(Typeface.DEFAULT);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int unsignedMin = mSeekBarMin < 0 ? mSeekBarMin * -1 : mSeekBarMin;
        String progressText = String.valueOf(getProgress()+unsignedMin);
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(progressText, 0, progressText.length(), bounds);

        int leftPadding = getPaddingLeft() - getThumbOffset();
        int rightPadding = getPaddingRight() - getThumbOffset();
        int width = getWidth() - leftPadding - rightPadding;
        float progressRatio = (float) getProgress() / getMax();
        float thumbOffset = mThumbSize * (.5f - progressRatio);
        float thumbX = progressRatio * width + leftPadding + thumbOffset;
        float thumbY = getHeight() / 2f + bounds.height() / 2f - 26;
        canvas.drawText(progressText, thumbX, thumbY, mTextPaint);
    }

    public void setMix(int min){
        mSeekBarMin=min;
    }
}
