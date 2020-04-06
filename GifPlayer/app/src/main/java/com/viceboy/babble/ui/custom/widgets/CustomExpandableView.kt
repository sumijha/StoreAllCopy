package com.viceboy.babble.ui.custom.widgets

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.viceboy.babble.R
import kotlinx.android.synthetic.main.custom_expandable_view.view.*

class CustomExpandableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    resStyleDef: Int = 0
) : LinearLayout(
    context,
    attrs,
    resStyleDef
) {
    var innerView: View? = null

    private var isExpanded: Boolean = false
    private var title: String? = null
    private var iconRes: Drawable? = null
    private var customBg: Drawable? = null
    private var collapseOnStart: Boolean? = false
    private var innerExpandedView: Int = 0

    private val defaultClickListener = OnClickListener {
        if (isExpanded)
            collapse()
        else
            expand()

    }

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_expandable_view, this)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomExpandableView)
            title = typedArray.getString(R.styleable.CustomExpandableView_headerText)
            iconRes = typedArray.getDrawable(R.styleable.CustomExpandableView_iconRes)
            customBg = typedArray.getDrawable(R.styleable.CustomExpandableRecyclerView_customBg)
            collapseOnStart =
                typedArray.getBoolean(R.styleable.CustomExpandableView_collapseOnStart, true)
            innerExpandedView = typedArray.getResourceId(
                R.styleable.CustomExpandableView_inner_expanded_view,
                View.NO_ID
            )
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!TextUtils.isEmpty(title)) tvExpandableViewHeader.text = title

        iconRes?.let {
            ivCustomResIcon.background = it
        }

        root_custom_expandedView.background = customBg

        view_stub.layoutResource = innerExpandedView
        innerView = view_stub.inflate()

        isExpanded = false
        rootView.setOnClickListener(defaultClickListener)
    }

    private fun collapse() {
        val initialHeight = container_expanded_view.measuredHeight
        val newHeight = root_custom_expandedView.measuredHeight
        if (initialHeight != newHeight) {
            animateLayout(newHeight, initialHeight, 180f, 0f)
        }
        isExpanded = false
    }

    private fun expand() {
        root_custom_expandedView.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val initialHeight = container_expanded_view.measuredHeight
        val newHeight = root_custom_expandedView.measuredHeight

        if (initialHeight != newHeight) {
            animateLayout(initialHeight, newHeight, 0f, 180f)
            isExpanded = true
        }
    }

    private fun animateLayout(
        startHeight: Int,
        finalHeight: Int,
        startRotation: Float,
        finalRotation: Float
    ) {
        val animatorHeight = ValueAnimator.ofInt(startHeight, finalHeight).apply {
            addUpdateListener {
                root_custom_expandedView.layoutParams.height = it.animatedValue as Int
                requestLayout()
                invalidate()
            }
        }

        val animatorRotation = ValueAnimator.ofFloat(startRotation, finalRotation).apply {
            addUpdateListener {
                ivDropdown.rotation = it.animatedValue as Float
            }

        }

        AnimatorSet().apply {
            duration = 200
            interpolator = LinearOutSlowInInterpolator()
            playTogether(animatorHeight, animatorRotation)
        }.start()
    }
}