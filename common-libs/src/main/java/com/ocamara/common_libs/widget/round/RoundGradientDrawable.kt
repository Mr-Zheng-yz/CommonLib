package com.ocamara.common_libs.widget.round

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.ocamara.common_libs.R

/**
 * res/drawable 中的shape文件动态设置
 */
class RoundGradientDrawable : GradientDrawable() {
    private var mStrokeWidth = 0
    var enableClickEffect = false

    /**
     * 设置描边宽度和颜色
     */
    fun setStrokeData(width: Int, color: Int) {
        mStrokeWidth = width
        setStroke(width, color)
    }

    fun setStrokeColor(color: Int) {
        setStrokeData(mStrokeWidth, color)
    }

    companion object {
        @JvmStatic
        fun fromAttrSet(context: Context, attrs: AttributeSet?, defStyleAttr: Int): RoundGradientDrawable {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundView, defStyleAttr, 0)
            val bgColor = typedArray.getColor(R.styleable.RoundView_roundBgColor, ContextCompat.getColor(context, android.R.color.transparent))
            val mRadius = typedArray.getDimensionPixelSize(R.styleable.RoundView_roundRadius, 0)
            val mTopLeftRadius = typedArray.getDimensionPixelSize(R.styleable.RoundView_topLeftRadius, 0)
            val mTopRightRadius = typedArray.getDimensionPixelSize(R.styleable.RoundView_topRightRadius, 0)
            val mBottomLeftRadius = typedArray.getDimensionPixelSize(R.styleable.RoundView_bottomLeftRadius, 0)
            val mBottomRightRadius = typedArray.getDimensionPixelSize(R.styleable.RoundView_bottomRightRadius, 0)
            val strokeColor = typedArray.getColor(R.styleable.RoundView_roundStrokeColor, ContextCompat.getColor(context, android.R.color.transparent))
            val strokeWidth = typedArray.getDimensionPixelSize(R.styleable.RoundView_roundStrokeWidth, 0)
            //渐变颜色
            val gradientType = typedArray.getInt(R.styleable.RoundView_gradientType, 0)
            val endColor = typedArray.getColor(R.styleable.RoundView_endColor, Color.TRANSPARENT)
            val startColor = typedArray.getColor(R.styleable.RoundView_startColor, Color.TRANSPARENT)
            val enableClickEffect = typedArray.getBoolean(R.styleable.RoundView_enableClickEffect, false)

            typedArray.recycle()
            val roundButtonDrawable = RoundGradientDrawable()

            roundButtonDrawable.enableClickEffect = enableClickEffect
            //设置背景颜色
            roundButtonDrawable.setColor(bgColor)
            //优先设置指定的圆角
            if (mTopLeftRadius > 0 || mTopRightRadius > 0 || mBottomLeftRadius > 0 || mBottomRightRadius > 0) {
                val radii = floatArrayOf(
                        mTopLeftRadius.toFloat(), mTopLeftRadius.toFloat(),
                        mTopRightRadius.toFloat(), mTopRightRadius.toFloat(),
                        mBottomRightRadius.toFloat(), mBottomRightRadius.toFloat(),
                        mBottomLeftRadius.toFloat(), mBottomLeftRadius
                        .toFloat())
                roundButtonDrawable.cornerRadii = radii
            } else {
                roundButtonDrawable.cornerRadius = mRadius.toFloat()
            }
            //设置描边的宽度和颜色
            roundButtonDrawable.setStrokeData(strokeWidth, strokeColor)

            //设置渐变背景
            if (endColor != 0 && startColor != 0) {
                roundButtonDrawable.orientation = getGradientOrientation(gradientType)
                roundButtonDrawable.colors = intArrayOf(startColor, endColor)
            }
            return roundButtonDrawable
        }

        private fun getGradientOrientation(gradientType: Int): GradientDrawable.Orientation {
            var orientation = Orientation.TOP_BOTTOM
            when (gradientType) {
                OrientationCustom.TOP_BOTTOM -> orientation = Orientation.TOP_BOTTOM
                OrientationCustom.TR_BL -> orientation = Orientation.TR_BL
                OrientationCustom.RIGHT_LEFT -> orientation = Orientation.RIGHT_LEFT
                OrientationCustom.BR_TL -> orientation = Orientation.BR_TL
                OrientationCustom.BOTTOM_TOP -> orientation = Orientation.BOTTOM_TOP
                OrientationCustom.BL_TR -> orientation = Orientation.BL_TR
                OrientationCustom.LEFT_RIGHT -> orientation = Orientation.LEFT_RIGHT
                OrientationCustom.TL_BR -> orientation = Orientation.TL_BR
            }
            return orientation
        }

        fun covertToRippleDrawable(roundGradientDrawable:RoundGradientDrawable?): RippleDrawable {
            val stateList = arrayOf(
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused),
                intArrayOf(android.R.attr.state_activated),
                intArrayOf()
            )
            val normalColor = Color.parseColor("#00000000")
            val pressedColor = Color.parseColor("#30000000")
            val stateColorList = intArrayOf(
                pressedColor,
                normalColor,
                normalColor,
                normalColor
            )
            val colorStateList = ColorStateList(stateList, stateColorList)
            return RippleDrawable(colorStateList, roundGradientDrawable, null)
        }
    }
}