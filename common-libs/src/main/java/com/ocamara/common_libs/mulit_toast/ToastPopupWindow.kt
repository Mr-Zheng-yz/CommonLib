package com.ocamara.common_libs.mulit_toast

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.view.children
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.ocamara.common_libs.R
import com.ocamara.common_libs.abs.IFunctionListener
import org.w3c.dom.Text

class ToastPopupWindow(
    private val activity: ComponentActivity,
    private val dismissListener: IFunctionListener<String>
) : PopupWindow(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
    DefaultLifecycleObserver {
    private val toastsArea = LinearLayout(activity).apply {
        orientation = LinearLayout.VERTICAL
        setFocusable(false)
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private val handler = Handler(Looper.getMainLooper())
    private val dismissRunnable = Runnable {
        if (toastsArea.childCount == 0) {
            dismiss()
            dismissListener.invoke("超时销毁")
        } else {
            Log.d("ToastUtil", "存在常驻Toast!!")
        }
    }

    init {
        activity.lifecycle.addObserver(this)
        contentView = toastsArea
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        setFocusable(false)
        setOutsideTouchable(false)
        setOnDismissListener {
            Log.d("ToastUtil", "--ToastPopupWindow销毁...")
        }

        val rootView = activity.window.decorView
        showAtLocation(rootView, Gravity.CENTER, 0, 0)
        Log.d("ToastUtil", "--创建ToastPopupWindow...")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (isShowing) {
            dismiss()
        }
        dismissListener.invoke("Activity销毁")
    }

    private fun resetDismiss(duration: Long) {
        handler.removeCallbacks(dismissRunnable)
        handler.postDelayed(dismissRunnable, duration + 160)
    }

    fun showTempToast(
        message: String,
        @ColorInt textColor: Int,
        @DrawableRes background: Int,
        @DrawableRes icon: Int,
        doNotTint: Boolean = false,
        duration: Long = 4000
    ) {
        resetDismiss(duration)
        val toast = ToastUtils.getToast(
            activity,
            toastsArea,
            message,
            textColor,
            background,
            icon,
            doNotTint,
        )
        toastsArea.addView(toast.root)
        toast.root.slideInToastFromTopForDuration(
            toastsArea,
            activity.lifecycleScope,
            duration
        )
    }

    fun showPersistentToast(
        message: String,
        @ColorInt textColor: Int,
        @DrawableRes background: Int,
        @DrawableRes icon: Int,
        doNotTint: Boolean = false,
        tag: String,
    ) {
        handler.removeCallbacks(dismissRunnable)
        val toastView = isContainsTagView(tag)
        if (toastView != null) {
            toastView.findViewById<TextView>(R.id.toast_message)?.let {
                it.text = message
                return
            }
        }
        val toast = ToastUtils.getToast(
            activity,
            toastsArea,
            message,
            textColor,
            background,
            icon,
            doNotTint,
        )
        toast.root.tag = tag
        toastsArea.addView(toast.root)
        toast.root.slideInToastFromTop(
            toastsArea,
            true
        )
    }

    fun removePersistentRedToast(tag: String) {
        for (child in toastsArea.children) {
            if (child.tag == tag) {
                toastsArea.removeView(child)
            }
        }
        resetDismiss(0)
    }

    private fun isContainsTagView(tag: String): View? {
        for (child in toastsArea.children) {
            if (child.tag == tag) {
                return child
            }
        }
        return null
    }

}