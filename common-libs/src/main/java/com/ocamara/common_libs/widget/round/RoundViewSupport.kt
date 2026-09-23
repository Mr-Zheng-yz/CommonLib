package com.ocamara.common_libs.widget.round

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View

/**
 * 圆角背景能力接口。
 * View 通过 `RoundViewSupport by RoundViewSupportDelegate(context, attrs, defStyleAttr)` 接入，
 * 并在 init 中调用 [attach] 完成背景装配，无需再逐个转发模板方法。
 */
interface RoundViewSupport {
  val roundDrawable: RoundGradientDrawable

  /** 装配背景到目标 View，需在 View 的 init 中调用（类委托表达式拿不到 this） */
  fun attach(view: View)

  /** 设置背景颜色（供 View 的 setBackgroundColor 转发调用） */
  fun setBgColor(color: Int)

  /** 设置描边的宽度和颜色 */
  fun setStrokeData(width: Int, color: Int)

  /** 设置描边颜色 */
  fun setStrokeColors(color: Int)

  /** 设置四个角的统一半径 */
  fun setRadius(radius: Int)

  /** 分别设置每个角的半径 */
  fun setEachCornerRadius(
    topLeftRadius: Int, topRightRadius: Int, bottomLeftRadius: Int, bottomRightRadius: Int
  )

  /** 设置渐变 */
  fun setGradient(
    gradientType: Int, orientation: GradientDrawable.Orientation?, colors: IntArray?
  )
}

/**
 * [RoundViewSupport] 的委托实现，持有并装配 [RoundGradientDrawable]。
 */
class RoundViewSupportDelegate(
  context: Context,
  attrs: AttributeSet?,
  defStyleAttr: Int
) : RoundViewSupport {

  override val roundDrawable: RoundGradientDrawable =
    RoundGradientDrawable.fromAttrSet(context, attrs, defStyleAttr)

  override fun attach(view: View) {
    if (roundDrawable.enableClickEffect) {
      view.background = RoundGradientDrawable.covertToRippleDrawable(roundDrawable)
    } else {
      ViewHelperUtil.setBackgroundKeepingPadding(view, roundDrawable)
    }
  }

  override fun setBgColor(color: Int) {
    roundDrawable.setColor(color)
  }

  override fun setStrokeData(width: Int, color: Int) {
    roundDrawable.setStrokeData(width, color)
  }

  override fun setStrokeColors(color: Int) {
    roundDrawable.setStrokeColor(color)
  }

  override fun setRadius(radius: Int) {
    roundDrawable.cornerRadius = radius.toFloat()
  }

  override fun setEachCornerRadius(
    topLeftRadius: Int, topRightRadius: Int, bottomLeftRadius: Int, bottomRightRadius: Int
  ) {
    roundDrawable.cornerRadii = floatArrayOf(
      topLeftRadius.toFloat(), topLeftRadius.toFloat(),
      topRightRadius.toFloat(), topRightRadius.toFloat(),
      bottomRightRadius.toFloat(), bottomRightRadius.toFloat(),
      bottomLeftRadius.toFloat(), bottomLeftRadius.toFloat()
    )
  }

  override fun setGradient(
    gradientType: Int, orientation: GradientDrawable.Orientation?, colors: IntArray?
  ) {
    roundDrawable.apply {
      this.gradientType = gradientType
      this.orientation = orientation
      this.colors = colors
    }
  }
}
