package com.ocamara.common_libs.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.ocamara.common_libs.R;

public class RatioFrameLayout extends FrameLayout {

    private float mRatioWidth;
    private float mRatioHeight;

    public RatioFrameLayout(Context context) {
        super(context);
        init(context, null);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RatioFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RatioFrameLayout);
        mRatioWidth = ta.getFloat(R.styleable.RatioFrameLayout_ratio_width, 0f);
        mRatioHeight = ta.getFloat(R.styleable.RatioFrameLayout_ratio_height, 0f);
        ta.recycle();
    }

    /** 设置比例，例如 16:9 → setRatio(16,9) */
    public void setRatio(float ratioW, float ratioH) {
        mRatioWidth = ratioW;
        mRatioHeight = ratioH;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 没有设置比例，直接走原生
        if (mRatioWidth <= 0 || mRatioHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int finalW = widthSize;
        int finalH = heightSize;

        // 场景1：宽度确定，高度wrap_content → 根据比例计算高度
        if (widthMode != MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED) {
            finalH = (int) (finalW * mRatioHeight / mRatioWidth);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(finalH, MeasureSpec.EXACTLY);
        }
        // 场景2：高度确定，宽度wrap_content → 根据比例计算宽度
        else if (heightMode != MeasureSpec.UNSPECIFIED && widthMode == MeasureSpec.UNSPECIFIED) {
            finalW = (int) (finalH * mRatioWidth / mRatioHeight);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(finalW, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
