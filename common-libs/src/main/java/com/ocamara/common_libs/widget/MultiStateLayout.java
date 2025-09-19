package com.ocamara.common_libs.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.ocamara.common_libs.R;

public class MultiStateLayout extends FrameLayout {
    private View contentView, loadingView, emptyView, errorView;
    private int emptyLayoutId, errorLayoutId;
    private OnRetryListener retryListener;

    public MultiStateLayout(Context context) {
        this(context, null);
    }

    public MultiStateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiStateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MultiStateLayout);
        emptyLayoutId = a.getResourceId(R.styleable.MultiStateLayout_mslEmptyLayout, R.layout.layout_empty);
        errorLayoutId = a.getResourceId(R.styleable.MultiStateLayout_mslErrorLayout, R.layout.layout_error);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("MultiStateLayout can host only one direct child");
        }
        contentView = getChildAt(0);
    }

    public void showLoading() {
        if (loadingView == null) {
            loadingView = inflateLayout(R.layout.layout_loading);
            addView(loadingView);
        }
        showView(loadingView);
    }

    public void showContent() {
        showView(contentView);
    }

    public void showEmpty() {
        if (emptyView == null) {
            emptyView = inflateLayout(emptyLayoutId);
            addView(emptyView);
        }
        showView(emptyView);
    }

    public void showError() {
        if (errorView == null) {
            errorView = inflateLayout(errorLayoutId);
            setupRetryButton(errorView);
            addView(errorView);
        }
        showView(errorView);
    }

    private void showView(View targetView) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setVisibility(child == targetView ? VISIBLE : GONE);
        }
    }

    private View inflateLayout(int layoutId) {
        return LayoutInflater.from(getContext()).inflate(layoutId, this, false);
    }

    private void setupRetryButton(View errorView) {
        View retryBtn = errorView.findViewById(R.id.retry);
        if (retryBtn != null) {
            retryBtn.setOnClickListener(v -> {
                if (retryListener != null) retryListener.onRetry();
            });
        }
    }

    public void setRetryListener(OnRetryListener listener) {
        this.retryListener = listener;
    }

    public interface OnRetryListener {
        void onRetry();
    }
}