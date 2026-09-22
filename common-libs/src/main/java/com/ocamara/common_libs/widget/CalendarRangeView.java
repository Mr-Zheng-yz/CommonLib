package com.ocamara.common_libs.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.ocamara.common_libs.utils.LogUtil;
import com.ocamara.common_libs.utils.SizeUtils;

import java.util.Calendar;

/**
 * 日期范围选择自定义 View
 * <p>
 * 功能：
 * 1. 显示当前月份的 6 行 × 7 列日历网格（包含上下月补位）
 * 2. 点击日期选择；月份切换由外部按钮调用 goToPrevMonth / goToNextMonth
 * 3. 绘制范围背景：起始日深蓝+左侧圆角、结束日深蓝+右侧圆角、中间日浅蓝纯色
 * <p>
 * 依赖 java.util.Calendar，minSdk 24 即兼容
 */
public class CalendarRangeView extends View {

    public interface OnDateClickListener {
        void onDateClick(Calendar date);
    }

    public interface OnMonthChangeListener {
        void onMonthChanged(Calendar firstDayOfMonth);
    }

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int TOTAL_CELLS = ROWS * COLS;

    private static final float TEXT_SIZE_SP = 14f;
    private static final float CORNER_RADIUS_DP = 8f;
    private static final float CELL_HEIGHT_DP = 44f;

    private static final int COLOR_TEXT = 0xFF333333;
    private static final int COLOR_SELECTED_TEXT = 0xFFFFFFFF;
    private static final int COLOR_RANGE_END = 0xFF1A6FE8;
    private static final int COLOR_RANGE_MIDDLE = 0xFFD6E6FF;

    private static final String TAG = "CalendarRangeView";

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF cellRect = new RectF();
    private final Path clipPath = new Path();

    private final Calendar displayedMonth = Calendar.getInstance();
    private Calendar startDate;
    private Calendar endDate;

    private final int[] cellYear = new int[TOTAL_CELLS];
    private final int[] cellMonth = new int[TOTAL_CELLS];
    private final int[] cellDay = new int[TOTAL_CELLS];
    private final boolean[] isCurrentMonth = new boolean[TOTAL_CELLS];

    private float cellWidth;
    private float cellHeight;
    private float cornerRadius;

    private OnDateClickListener onDateClickListener;
    private OnMonthChangeListener onMonthChangeListener;

    public CalendarRangeView(Context context) {
        this(context, null);
    }

    public CalendarRangeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarRangeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        cornerRadius = SizeUtils.dp2px(CORNER_RADIUS_DP);
        cellHeight = SizeUtils.dp2px(CELL_HEIGHT_DP);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(SizeUtils.sp2px(TEXT_SIZE_SP));
        textPaint.setColor(COLOR_TEXT);

        selectedTextPaint.setTextAlign(Paint.Align.CENTER);
        selectedTextPaint.setTextSize(SizeUtils.sp2px(TEXT_SIZE_SP));
        selectedTextPaint.setColor(COLOR_SELECTED_TEXT);

        rangePaint.setStyle(Paint.Style.FILL);

        setDisplayedMonth(Calendar.getInstance());
    }

    public void setOnDateClickListener(OnDateClickListener l) {
        this.onDateClickListener = l;
    }

    public void setOnMonthChangeListener(OnMonthChangeListener l) {
        this.onMonthChangeListener = l;
    }

    public void setRange(Calendar start, Calendar end) {
        this.startDate = start == null ? null : cloneDay(start);
        this.endDate = end == null ? null : cloneDay(end);
        invalidate();
    }

    public Calendar getStartDate() {
        return startDate == null ? null : (Calendar) startDate.clone();
    }

    public Calendar getEndDate() {
        return endDate == null ? null : (Calendar) endDate.clone();
    }

    public Calendar getDisplayedMonth() {
        return cloneDay(displayedMonth);
    }

    public void setDisplayedMonth(Calendar anyDayInMonth) {
        displayedMonth.setTimeInMillis(anyDayInMonth.getTimeInMillis());
        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        clearTimeOfDay(displayedMonth);
        rebuildCells();
        invalidate();
    }

    public void goToPrevMonth() {
        displayedMonth.add(Calendar.MONTH, -1);
        clearTimeOfDay(displayedMonth);
        rebuildCells();
        invalidate();
        notifyMonthChanged();
    }

    public void goToNextMonth() {
        displayedMonth.add(Calendar.MONTH, 1);
        clearTimeOfDay(displayedMonth);
        rebuildCells();
        invalidate();
        notifyMonthChanged();
    }

    private void notifyMonthChanged() {
        if (onMonthChangeListener != null) {
            onMonthChangeListener.onMonthChanged(cloneDay(displayedMonth));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        cellWidth = widthSize / (float) COLS;
        float theoretical = cellHeight * ROWS;
        int measuredHeight = (int) Math.ceil(theoretical);
        setMeasuredDimension(widthSize, measuredHeight);
        LogUtil.d(TAG, "onMeasure: cellHeight=" + cellHeight
                + " theoretical=" + theoretical
                + " ceil=" + measuredHeight
                + " | widthSpec=" + MeasureSpec.toString(widthMeasureSpec)
                + " heightSpec=" + MeasureSpec.toString(heightMeasureSpec)
                + " | reported=" + widthSize + "x" + measuredHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int theoretical = (int) Math.ceil(cellHeight * ROWS);
        String verdict = (h < theoretical)
                ? " [CLIPPED by parent, diff=" + (theoretical - h) + "]"
                : " [OK]";
        LogUtil.d(TAG, "onSizeChanged: actual=" + w + "x" + h
                + " | theoretical=" + theoretical
                + " (cellHeight=" + cellHeight + " x ROWS=" + ROWS + ")"
                + verdict);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cellWidth <= 0 || cellHeight <= 0) return;

        // 1) 绘制范围背景（在文字下层）
        if (startDate != null) {
            drawRange(canvas);
        }

        // 2) 绘制日期数字：仅本月日期
        for (int i = 0; i < TOTAL_CELLS; i++) {
            if (!isCurrentMonth[i]) continue;
            int row = i / COLS;
            int col = i % COLS;
            float cx = col * cellWidth + cellWidth / 2f;
            float baseline = row * cellHeight + cellHeight / 2f
                    - (textPaint.descent() + textPaint.ascent()) / 2f;

            Paint p = isCellInRange(i) ? selectedTextPaint : textPaint;
            canvas.drawText(String.valueOf(cellDay[i]), cx, baseline, p);
        }
    }

    /**
     * 绘制范围背景。
     * <p>
     * 关键：仅当「实际起始日」或「实际结束日」落在当前可视的当前月单元格里时，
     * 才绘制深蓝色+半圆角；其它 in-range 单元一律走浅蓝中间态。
     * 这样跨月选范围时，本月内的"首/末可见格"不会被错误地涂成深蓝。
     */
    private void drawRange(Canvas canvas) {
        if (startDate == null) return;
        long startTs = startDate.getTimeInMillis();
        long endTs = (endDate != null ? endDate : startDate).getTimeInMillis();

        int firstInRange = -1;
        int lastInRange = -1;
        int startCellIdx = -1;  // 实际起始日命中的单元索引
        int endCellIdx = -1;    // 实际结束日命中的单元索引
        for (int i = 0; i < TOTAL_CELLS; i++) {
            if (!isCurrentMonth[i]) continue;
            long t = makeTimestamp(cellYear[i], cellMonth[i], cellDay[i]);
            if (t >= startTs && t <= endTs) {
                if (firstInRange < 0) firstInRange = i;
                lastInRange = i;
            }
            if (t == startTs) startCellIdx = i;
            if (t == endTs) endCellIdx = i;
        }
        if (firstInRange < 0) return; // 当前视图与所选范围无交集

        for (int i = firstInRange; i <= lastInRange; i++) {
            getCellRect(i, cellRect);
            if (i == startCellIdx && i == endCellIdx) {
                // 起止同一天：四角全圆
                drawRoundedRect(canvas, cellRect, cornerRadius,
                        true, true, true, true, COLOR_RANGE_END);
            } else if (i == startCellIdx) {
                // 实际起始日：左圆
                drawRoundedRect(canvas, cellRect, cornerRadius,
                        true, false, true, false, COLOR_RANGE_END);
            } else if (i == endCellIdx) {
                // 实际结束日：右圆
                drawRoundedRect(canvas, cellRect, cornerRadius,
                        false, true, false, true, COLOR_RANGE_END);
            } else {
                // 中间：实心浅蓝（含跨月时本月首/末的"伪端点"）
                rangePaint.setColor(COLOR_RANGE_MIDDLE);
                canvas.drawRect(cellRect, rangePaint);
            }
        }
    }

    private void drawRoundedRect(Canvas canvas, RectF rect, float r,
                                 boolean tl, boolean tr, boolean bl, boolean br, int color) {
        rangePaint.setColor(color);
        float rtl = tl ? r : 0;
        float rtr = tr ? r : 0;
        float rbl = bl ? r : 0;
        float rbr = br ? r : 0;
        float[] radii = {rtl, rtl, rtr, rtr, rbr, rbr, rbl, rbl};

        clipPath.reset();
        clipPath.addRoundRect(rect, radii, Path.Direction.CW);
        int saved = canvas.save();
        canvas.clipPath(clipPath);
        canvas.drawRect(rect, rangePaint);
        canvas.restoreToCount(saved);
    }

    private RectF getCellRect(int idx, RectF out) {
        int row = idx / COLS;
        int col = idx % COLS;
        out.set(col * cellWidth, row * cellHeight,
                (col + 1) * cellWidth, (row + 1) * cellHeight);
        return out;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // 显式声明消费事件，否则后续 ACTION_UP 不会派发到本 View
                return true;
            case MotionEvent.ACTION_UP:
                handleClick(event.getX(), event.getY());
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void handleClick(float x, float y) {
        if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) return;
        int col = (int) (x / cellWidth);
        int row = (int) (y / cellHeight);
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return;
        int idx = row * COLS + col;
        // 补位日期不响应点击
        if (!isCurrentMonth[idx]) return;
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(cellYear[idx], cellMonth[idx], cellDay[idx]);
        if (onDateClickListener != null) {
            onDateClickListener.onDateClick(c);
        }
    }

    private void rebuildCells() {
        Calendar firstOfMonth = (Calendar) displayedMonth.clone();
        firstOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = firstOfMonth.get(Calendar.DAY_OF_WEEK);
        int leadingEmpty = dayOfWeek - 1;
        Calendar cellCal = (Calendar) firstOfMonth.clone();
        cellCal.add(Calendar.DAY_OF_MONTH, -leadingEmpty);

        int targetYear = firstOfMonth.get(Calendar.YEAR);
        int targetMonth = firstOfMonth.get(Calendar.MONTH);
        for (int i = 0; i < TOTAL_CELLS; i++) {
            cellYear[i] = cellCal.get(Calendar.YEAR);
            cellMonth[i] = cellCal.get(Calendar.MONTH);
            cellDay[i] = cellCal.get(Calendar.DAY_OF_MONTH);
            isCurrentMonth[i] = (cellYear[i] == targetYear && cellMonth[i] == targetMonth);
            cellCal.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isCellInRange(int idx) {
        if (startDate == null) return false;
        if (endDate == null) {
            return cellYear[idx] == startDate.get(Calendar.YEAR)
                    && cellMonth[idx] == startDate.get(Calendar.MONTH)
                    && cellDay[idx] == startDate.get(Calendar.DAY_OF_MONTH);
        }
        long t = makeTimestamp(cellYear[idx], cellMonth[idx], cellDay[idx]);
        return t >= startDate.getTimeInMillis() && t <= endDate.getTimeInMillis();
    }

    private static long makeTimestamp(int y, int m, int d) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(y, m, d);
        return c.getTimeInMillis();
    }

    private static Calendar cloneDay(Calendar src) {
        Calendar c = (Calendar) src.clone();
        clearTimeOfDay(c);
        return c;
    }

    private static void clearTimeOfDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }
}
