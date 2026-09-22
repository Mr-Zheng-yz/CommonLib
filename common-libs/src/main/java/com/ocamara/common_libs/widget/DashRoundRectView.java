package com.ocamara.common_libs.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;

import com.ocamara.common_libs.R;

public class DashRoundRectView extends View {
    private final Paint borderPaint = new Paint();
    private final Path path = new Path();
    private final RectF rectF = new RectF();

    private float cornerRadius = 16f;
    private float dashLineLength = 8f;
    private float dashGap = 4f;
    private int borderColor = Color.BLACK;
    private float borderWidth = 2f;

    public DashRoundRectView(Context context) {
        super(context);
        init(null);
    }

    public DashRoundRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DashRoundRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);

        if (attrs != null) {
            final int[] styleable = R.styleable.DashRoundRectView;
            var ta = getContext().obtainStyledAttributes(attrs, styleable);
            cornerRadius = ta.getDimension(R.styleable.DashRoundRectView_drr_cornerRadius, 16f);
            dashLineLength = ta.getDimension(R.styleable.DashRoundRectView_drr_dashLineLength, 8f);
            dashGap = ta.getDimension(R.styleable.DashRoundRectView_drr_dashGap, 4f);
            borderColor = ta.getColor(R.styleable.DashRoundRectView_drr_borderColor, Color.BLACK);
            borderWidth = ta.getDimension(R.styleable.DashRoundRectView_drr_borderWidth, 2f);
            ta.recycle();
        }
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);
        updateDashEffect();
    }

    private void updateDashEffect() {
        borderPaint.setPathEffect(new DashPathEffect(new float[]{dashLineLength, dashGap}, 0f));
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setDashLineLength(float dashLineLength) {
        this.dashLineLength = dashLineLength;
        updateDashEffect();
    }

    public void setDashGap(float dashGap) {
        this.dashGap = dashGap;
        updateDashEffect();
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
        borderPaint.setColor(borderColor);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
        borderPaint.setStrokeWidth(borderWidth);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float pl = getPaddingLeft();
        float pt = getPaddingTop();
        float pr = getPaddingRight();
        float pb = getPaddingBottom();

        rectF.set(
                pl + borderWidth / 2f,
                pt + borderWidth / 2f,
                w - pr - borderWidth / 2f,
                h - pb - borderWidth / 2f
        );
        refreshPath();
    }

    private void refreshPath() {
        path.reset();
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, borderPaint);
    }
}
