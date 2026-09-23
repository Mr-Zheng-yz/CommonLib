package com.ocamara.common_libs.widget.round.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.ocamara.common_libs.widget.round.RoundViewSupport
import com.ocamara.common_libs.widget.round.RoundViewSupportDelegate

/**
 * 可以设置背景色、指定圆角、描边的宽度和颜色
 *
 * 圆角背景能力由 [com.ocamara.common_libs.widget.round.RoundViewSupport] 委托实现，自定义 API 无需在此逐个转发。
 */
class RoundRelativeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr),
  RoundViewSupport by RoundViewSupportDelegate(context, attrs, defStyleAttr) {

  init {
    attach(this)
  }

  /**
   * setBackgroundColor 是 View 的平台方法，类委托无法覆盖，这里保留一行转发
   */
  override fun setBackgroundColor(color: Int) {
    setBgColor(color)
  }
}