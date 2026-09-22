package com.ocamara.common_libs.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.ocamara.common_libs.R;
import com.ocamara.common_libs.utils.UiHelper;

import org.jetbrains.annotations.Nullable;

/**
 * 轻量加载等待弹窗。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>使用普通 {@link Dialog} + 自绘布局 (R.layout.dialog_loading)，
 *       避免 {@code ProgressDialog} 自 API 29 起被 deprecated 带来的额外 AlertDialog 框架开销。</li>
 *   <li>唯一性：以 TAG 查找 {@link FragmentManager} 中是否已存在实例，
 *       存在则先 dismiss 再 show，调用方需传入 Activity 级的 FragmentManager
 *       (推荐通过 {@code BaseActivity#showLoading(...)} 调用)。</li>
 *   <li>超时：默认 10s，可通过 {@link #newInstance(String, long)} 自定义，
 *       超时后自动 dismiss 并回调 {@link OnTimeoutListener}。</li>
 * </ul>
 */
public class LoadingDialogFragment extends DialogFragment {
    private static final String TAG = "LoadingDialogFragment";

    private Handler handler;
    private OnTimeoutListener onTimeoutListener;
    private TextView tvMessage;

    public static LoadingDialogFragment newInstance(String message) {
        return newInstance(message, 10000L);
    }

    public static LoadingDialogFragment newInstance(String message, long timeoutMillis) {
        LoadingDialogFragment fragment = new LoadingDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        args.putLong("timeout", timeoutMillis);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = getArguments() != null ? getArguments().getString("message", "") : "";
        long timeout = getArguments() != null ? getArguments().getLong("timeout", 10_000L) : 10_000L;

        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null, false);
        tvMessage = view.findViewById(R.id.tv_loading_message);
        if (TextUtils.isEmpty(message)) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        dialog.setContentView(view);

        // 窗口属性：透明背景 + WRAP_CONTENT + 居中，避免系统默认内边距
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            lp.dimAmount = 0.4f;
            window.setAttributes(lp);
        }

        // 超时自动 dismiss
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (getDialog() != null && getDialog().isShowing()) {
                dismissAllowingStateLoss();
                if (onTimeoutListener != null) {
                    onTimeoutListener.onTimeout();
                }
            }
        }, timeout);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            UiHelper.hideNavigationBar(dialog.getWindow());
        }
    }

    @Override
    public void onDestroyView() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        super.onDestroyView();
    }

    public interface OnTimeoutListener {
        void onTimeout();
    }

    public void setOnTimeoutListener(OnTimeoutListener listener) {
        this.onTimeoutListener = listener;
    }

    /**
     * 显示加载弹窗。保证传入的 FragmentManager 作用域内唯一。
     * 推荐传入 Activity 级 FragmentManager。
     */
    public static void showLoading(FragmentManager manager, String message) {
        hideLoading(manager);
        LoadingDialogFragment dialog = LoadingDialogFragment.newInstance(message, 60_000L);
        dialog.show(manager, TAG);
    }

    /**
     * 显示加载弹窗并设置超时。
     */
    public static void showLoading(FragmentManager manager, String message, long maxTime, OnTimeoutListener listener) {
        hideLoading(manager);
        LoadingDialogFragment dialog = LoadingDialogFragment.newInstance(message, maxTime);
        dialog.setOnTimeoutListener(listener);
        dialog.show(manager, TAG);
    }

    /**
     * 隐藏已显示的加载弹窗。无害：未显示时直接返回。
     */
    public static void hideLoading(FragmentManager manager) {
        if (manager.isStateSaved() || manager.isDestroyed()) {
            return;
        }
        // show() 的事务是异步提交的，先强制执行挂起的事务，
        // 否则 show 后立即 hide 时 findFragmentByTag 查不到实例，导致弹窗无法取消
        manager.executePendingTransactions();
        Fragment fragment = manager.findFragmentByTag(TAG);
        if (fragment instanceof LoadingDialogFragment) {
            ((LoadingDialogFragment) fragment).dismissAllowingStateLoss();
        }
    }
}
