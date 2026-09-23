package com.ocamara.common_libs.widget.round.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.ocamara.common_libs.widget.round.RoundViewSupport
import com.ocamara.common_libs.widget.round.RoundViewSupportDelegate

class RoundEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    // EditText 的 focusable/clickable 等关键默认值来自主题的 editTextStyle，
    // 默认给 0 会丢失整个默认样式，导致不可聚焦、键盘无法唤起
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr),
    RoundViewSupport by RoundViewSupportDelegate(context, attrs, defStyleAttr) {

    init {
        attach(this)
    }

    override fun setBackgroundColor(color: Int) {
        setBgColor(color)
    }

}