package com.viceboy.babble.ui.custom.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.viceboy.babble.R
import kotlinx.android.synthetic.main.custom_error_view.view.*

class CustomErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    resStyleDef: Int = 0
) : LinearLayout(context, attrs, resStyleDef) {

    private var errorTitle: String? = null
    private var errorIcon: Drawable? = null
    private var errorTextColor : Int = ContextCompat.getColor(context,android.R.color.black)
    private var strokeColor: Int = 0
    private var strokeWidth: Float = 0f
    private var errorBackground: Int = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_error_view, this)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomErrorView)
            errorTitle = typedArray.getString(R.styleable.CustomErrorView_errorMsg)
            errorIcon = typedArray.getDrawable(R.styleable.CustomErrorView_errorIcon)
            errorBackground = typedArray.getColor(R.styleable.CustomErrorView_iconBackground,resStyleDef)
            errorTextColor = typedArray.getColor(R.styleable.CustomErrorView_errorTextColor,resStyleDef)
            strokeColor = typedArray.getColor(R.styleable.CustomErrorView_errorStrokeColor, resStyleDef)
            strokeWidth = typedArray.getDimension(R.styleable.CustomErrorView_errorStrokeWidth,0f)
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!errorTitle.isNullOrEmpty()) {
            tvErrorMsg.apply {
                text = errorTitle
                setTextColor(errorTextColor)
            }
        }

        val rootBg = tvErrorMsg.background as GradientDrawable
        rootBg.setStroke(strokeWidth.toInt(), strokeColor)
        tvErrorMsg.background = rootBg

        val imageBg = ivErrorIcon.background as GradientDrawable
        imageBg.setColor(errorBackground)

        errorBackground.let {
            ivErrorIcon.setImageDrawable(errorIcon)
            ivErrorIcon.background = imageBg
        }
    }
}