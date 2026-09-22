package com.ocamara.common_libs.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ocamara.common_libs.R;

public class DashLineView extends View {

    private Paint mPaint;
    private Path mPath;
    private DashPathEffect mDashPathEffect;

    // 属性
    private int mLineColor;
    private float mDashLength;   // 虚线线段长度 dp
    private float mDashGap;      // 虚线间隔长度 dp
    private float mLineWidth;    // 线条粗细 dp
    private boolean mIsVertical; // 是否竖向虚线 true=竖向 false=横向

    private float mDensity;

    public DashLineView(Context context) {
        this(context, null);
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDensity = getResources().getDisplayMetrics().density;

        // 读取xml自定义属性
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DashLineView);
        mLineColor = ta.getColor(R.styleable.DashLineView_dash_color, 0xFFCCCCCC);
        mDashLength = ta.getDimension(R.styleable.DashLineView_dash_length, dp2px(4));
        mDashGap = ta.getDimension(R.styleable.DashLineView_dash_gap, dp2px(3));
        mLineWidth = ta.getDimension(R.styleable.DashLineView_line_width, dp2px(1));
        mIsVertical = ta.getBoolean(R.styleable.DashLineView_is_vertical, true);
        ta.recycle();

        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.BUTT);

        mPath = new Path();
        // DashPathEffect(线段长度,间隔长度,偏移量)
        mDashPathEffect = new DashPathEffect(new float[]{mDashLength, mDashGap}, 0);
        mPaint.setPathEffect(mDashPathEffect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPath.reset();

        if (mIsVertical) {
            // 竖向虚线：从上到下
            int centerX = getWidth() / 2;
            mPath.moveTo(centerX, 0);
            mPath.lineTo(centerX, getHeight());
        } else {
            // 横向虚线：从左到右
            int centerY = getHeight() / 2;
            mPath.moveTo(0, centerY);
            mPath.lineTo(getWidth(), centerY);
        }
        canvas.drawPath(mPath, mPaint);
    }

    // =========对外暴露设置方法，代码动态修改=========
    public void setDashColor(int color) {
        mLineColor = color;
        mPaint.setColor(mLineColor);
        invalidate();
    }

    /**
     * @param dashDp 虚线实线部分长度 dp
     * @param gapDp 虚线间隔 dp
     */
    public void setDashParam(float dashDp, float gapDp) {
        mDashLength = dp2px(dashDp);
        mDashGap = dp2px(gapDp);
        mDashPathEffect = new DashPathEffect(new float[]{mDashLength, mDashGap}, 0);
        mPaint.setPathEffect(mDashPathEffect);
        invalidate();
    }

    public void setLineWidth(float widthDp) {
        mLineWidth = dp2px(widthDp);
        mPaint.setStrokeWidth(mLineWidth);
        invalidate();
    }

    public void setVertical(boolean vertical) {
        mIsVertical = vertical;
        invalidate();
    }

    private float dp2px(float dp) {
        return dp * mDensity;
    }
}
