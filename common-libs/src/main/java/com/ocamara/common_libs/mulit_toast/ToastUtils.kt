package com.ocamara.common_libs.mulit_toast

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.MainThread
import androidx.annotation.UiThread
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.ocamara.common_libs.R
import com.ocamara.common_libs.databinding.ToastBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val popupWindowHashMap = HashMap<ComponentActivity, ToastPopupWindow>()

private fun ensurePopupWindow(activity: ComponentActivity): ToastPopupWindow {
    var cachePopupWindow = popupWindowHashMap[activity]
    if (cachePopupWindow == null) {
        cachePopupWindow = ToastPopupWindow(activity) { msg ->
            popupWindowHashMap.remove(activity)
            Log.d("ToastUtil", "--移除PopupWindow:$msg")
        }
        popupWindowHashMap[activity] = cachePopupWindow
    }
    return cachePopupWindow
}

@UiThread
fun ComponentActivity.showRedToast(
    message: String,
    duration: Long = 4000,
) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@showRedToast).showTempToast(
                message,
                Color.parseColor("#DD5F5F"),
                R.drawable.shape_toast_red_background,
                R.drawable.warning_circle,
                false,
                duration
            )
        }
    }
}

@UiThread
fun ComponentActivity.showBlueToast(
    message: String,
    duration: Long = 4000,
) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@showBlueToast).showTempToast(
                message,
                Color.parseColor("#4AA8FF"),
                R.drawable.shape_toast_blue_background,
                R.drawable.trusted,
                true,
                duration
            )
        }
    }
}

@UiThread
fun ComponentActivity.showGreenToast(
    message: String,
    duration: Long = 4000,
) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@showGreenToast).showTempToast(
                message,
                Color.parseColor("#4FAE80"),
                R.drawable.shape_toast_green_background,
                R.drawable.check,
                false,
                duration
            )
        }
    }
}

@UiThread
fun ComponentActivity.showCustomToast(
    message: String,
    duration: Long = 4000,
    @ColorInt textColor: Int,
    @DrawableRes background: Int,
    @DrawableRes icon: Int,
    doNotTint: Boolean = false
) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@showCustomToast).showTempToast(
                message,
                textColor,
                background,
                icon,
                doNotTint,
                duration
            )
        }
    }
}

@UiThread
fun ComponentActivity.showPersistentRedToast(
    message: String,
    tag: String,
) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@showPersistentRedToast).showPersistentToast(
                message,
                Color.parseColor("#DD5F5F"),
                R.drawable.shape_toast_red_background,
                R.drawable.warning_circle,
                false,
                tag
            )
        }
    }
}

@UiThread
fun ComponentActivity.removePersistentRedToast(tag: String) {
    lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            ensurePopupWindow(this@removePersistentRedToast).removePersistentRedToast(tag)
        }
    }
}

@UiThread
fun View.slideInToastFromTop(
    root: ViewGroup,
    visible: Boolean
) {
    val view = this
    val transition: Transition = Slide(Gravity.TOP)
    transition.duration = 600
    transition.addTarget(view)

    TransitionManager.beginDelayedTransition(root, transition)
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@UiThread
fun View.slideInToastFromTopForDuration(
    root: ViewGroup,
    lifecycleScope: LifecycleCoroutineScope,
    duration: Long = 4000
) {
    val view = this
    val transition: Transition = Slide(Gravity.TOP)
    transition.duration = 600
    transition.addTarget(view)

    TransitionManager.beginDelayedTransition(root, transition)
    view.visibility = View.VISIBLE

    lifecycleScope.launch {
        withContext(Dispatchers.IO) {
            delay(duration)
            withContext(Dispatchers.Main) {
                root.removeView(view)
            }
        }
    }
}

class ToastUtils {
    companion object {
        @MainThread
        fun getToast(
            context: Context,
            parent: ViewGroup,
            message: String,
            @ColorInt textColor: Int,
            @DrawableRes background: Int,
            @DrawableRes icon: Int,
            doNotTint: Boolean = false
        ): ToastBinding {
            val toast: ToastBinding = ToastBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
            toast.toastMessage.setTextColor(textColor)
            if (!doNotTint) {
                toast.toastIcon.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
            }
            toast.toastMessage.text = message
            toast.toastIcon.setImageResource(icon)
            toast.toastBackground.setImageResource(background)
            toast.root.visibility = View.GONE
            return toast
        }
    }
}
