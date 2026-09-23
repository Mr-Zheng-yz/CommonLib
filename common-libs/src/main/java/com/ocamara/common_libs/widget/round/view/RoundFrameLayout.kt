package com.ocamara.common_libs.widget.round.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.ocamara.common_libs.widget.round.RoundViewSupport
import com.ocamara.common_libs.widget.round.RoundViewSupportDelegate

class RoundFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr),
    RoundViewSupport by RoundViewSupportDelegate(context, attrs, defStyleAttr) {

    init {
        attach(this)
    }

    override fun setBackgroundColor(color: Int) {
        setBgColor(color)
    }

}