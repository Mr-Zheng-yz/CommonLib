package com.ocamara.common_libs.widget.round

import android.graphics.drawable.Drawable
import android.view.View

internal object ViewHelperUtil {
  @JvmStatic
  fun setBackgroundKeepingPadding(
    view: View,
    drawable: Drawable?
  ) {
    //        val padding = intArrayOf(view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
    view.background = drawable
    //        view.setPadding(padding[0], padding[1], padding[2], padding[3])
  }
}
