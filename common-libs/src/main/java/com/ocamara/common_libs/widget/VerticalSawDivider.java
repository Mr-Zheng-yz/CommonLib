package com.ocamara.common_libs.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.ocamara.common_libs.R;

public class VerticalSawDivider extends View {
    private final Paint mPaint;
    private float mSawRadius;    // 半圆锯齿半径
    private float mSawGap;       // 锯齿中心间距
    private int mSawColor;       // 锯齿分割线颜色

    public VerticalSawDivider(Context context) {
        this(context, null);
    }

    public VerticalSawDivider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalSawDivider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalSawDivider);
        mSawRadius = ta.getDimension(R.styleable.VerticalSawDivider_saw_radius, 8f);
        mSawGap = ta.getDimension(R.styleable.VerticalSawDivider_saw_gap, 24f);
        mSawColor = ta.getColor(R.styleable.VerticalSawDivider_saw_color, Color.WHITE);
        ta.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewHeight = getHeight();
        int viewWidth = getWidth();
        if (viewHeight <= 0 || viewWidth <= 0) return;

        Path path = new Path();
        mPaint.setColor(mSawColor);

        // 计算最多可以放下多少个完整锯齿
        int sawCount = (int) (viewHeight / mSawGap);
        // 保证锯齿数量为奇数，实现上下对称
        if (sawCount % 2 == 0) {
            sawCount -= 1;
        }
        // 计算总占用高度，算出顶部偏移，让锯齿整体居中
        float totalSawHeight = sawCount * mSawGap;
        float startY = (viewHeight - totalSawHeight) / 2f;

        float currentY = startY;
        boolean turn = true;
        path.moveTo(0, currentY);

        for (int i = 0; i < sawCount; i++) {
            RectF rect;
            if (turn) {
                // 向右凸半圆
                rect = new RectF(-mSawRadius, currentY, mSawRadius, currentY + mSawRadius * 2);
                path.arcTo(rect, 90, 180, false);
            } else {
                // 向左凹半圆
                rect = new RectF(-mSawRadius, currentY, mSawRadius, currentY + mSawRadius * 2);
                path.arcTo(rect, 270, 180, false);
            }
            currentY += mSawGap;
            turn = !turn;
        }
        path.lineTo(0, viewHeight);
        path.close();

        canvas.drawPath(path, mPaint);
    }

    // 对外暴露setter，动态修改锯齿参数
    public void setSawRadius(float radius) {
        mSawRadius = radius;
        invalidate();
    }

    public void setSawGap(float gap) {
        mSawGap = gap;
        invalidate();
    }

    public void setSawColor(int color) {
        mSawColor = color;
        invalidate();
    }
}
