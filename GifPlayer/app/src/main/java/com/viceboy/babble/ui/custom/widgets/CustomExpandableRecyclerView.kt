package com.viceboy.babble.ui.custom.widgets

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.ViewDataBinding
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.babble.ui.screens.addExpense.BaseDataBoundAdapter
import kotlinx.android.synthetic.main.custom_expandable_recycler_view.view.*

class CustomExpandableRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    resStyleDef: Int = 0
) : LinearLayout(
    context,
    attrs,
    resStyleDef
) {

    private var title: String? = null
    private var iconRes: Drawable? = null
    private var customBg: Drawable? = null
    private var customCollapsedViewListener: CustomCollapsedViewListener? = null
    private var collapseOnStart: Boolean? = false
    private var isExpanded: Boolean = false

    val isCollapsed
        get() = !isExpanded

    private val defaultClickListener = OnClickListener {
        if (rvCustomExpandableView.adapter?.itemCount ?: 0 > 0) {
            if (isExpanded)
                collapse()
            else
                expand()
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_expandable_recycler_view, this)
        attrs?.let {
            val typedArray =
                context.obtainStyledAttributes(attrs, R.styleable.CustomExpandableRecyclerView)
            title = typedArray.getString(R.styleable.CustomExpandableRecyclerView_headerTextRv)
            iconRes = typedArray.getDrawable(R.styleable.CustomExpandableRecyclerView_iconRvRes)
            customBg = typedArray.getDrawable(R.styleable.CustomExpandableRecyclerView_customBg)
            collapseOnStart =
                typedArray.getBoolean(
                    R.styleable.CustomExpandableRecyclerView_collapseOnStartRv,
                    true
                )
            typedArray.recycle()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!TextUtils.isEmpty(title)) tvExpandableViewRvHeader.text = title

        iconRes?.let {
            ivCustomResRvIcon.background = it
        }

        root_custom_expandedRecyclerView.background = customBg

        isExpanded = false
        rootView.setOnClickListener(defaultClickListener)
    }

    private fun collapse() {
        val initialHeight = container_expanded_RecyclerView.measuredHeight
        val newHeight = root_custom_expandedRecyclerView.measuredHeight
        if (initialHeight != newHeight) {
            animateLayout(newHeight, initialHeight, 180f, 0f)
        }
        rootView.clearFocus()
        isExpanded = false
    }


    fun setHeaderText(text: String) {
        if (text.isNotEmpty()) tvExpandableViewRvHeader.text = text
    }

    fun <T, V : ViewDataBinding> setRecyclerAdapter(adapter: DataBoundListAdapter<T, V>) {
        rvCustomExpandableView.adapter = adapter
    }

    fun <T> setRecyclerAdapter(adapter: BaseDataBoundAdapter<T>) {
        rvCustomExpandableView.adapter = adapter
    }

    fun setCollapsedViewListener(listener: CustomCollapsedViewListener) {
        customCollapsedViewListener = listener
    }

    fun collapseView() = collapse()


    private fun expand() {
        root_custom_expandedRecyclerView.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val initialHeight = container_expanded_RecyclerView.measuredHeight
        val newHeight = root_custom_expandedRecyclerView.measuredHeight

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
                root_custom_expandedRecyclerView.layoutParams.height = it.animatedValue as Int
                requestLayout()
                invalidate()
            }
        }

        val animatorRotation = ValueAnimator.ofFloat(startRotation, finalRotation).apply {
            addUpdateListener {
                ivRvDropdown.rotation = it.animatedValue as Float
            }

        }

        AnimatorSet().apply {
            duration = 200
            interpolator = LinearOutSlowInInterpolator()
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) = Unit

                override fun onAnimationEnd(animation: Animator?) {
                    if (finalHeight > startHeight)
                        customCollapsedViewListener?.onViewExpanded()
                    else
                        customCollapsedViewListener?.onViewCollapsed()
                }

                override fun onAnimationCancel(animation: Animator?) = Unit

                override fun onAnimationStart(animation: Animator?) {
                    customCollapsedViewListener?.onViewExpandStart()
                }

            })
            playTogether(animatorHeight, animatorRotation)
        }.start()
    }

    interface CustomCollapsedViewListener {
        fun onViewExpandStart()
        fun onViewExpanded()
        fun onViewCollapsed()
    }

    /*private fun animateLayoutIn(
        startHeight: Int,
        finalHeight: Int,
        startRotation: Float,
        finalRotation: Float
    ) {
        val animatorHeight = ValueAnimator.ofInt(startHeight, finalHeight).apply {
            addUpdateListener {
                root_custom_expandedRecyclerView.layoutParams.height = it.animatedValue as Int
                requestLayout()
                invalidate()
            }
        }

        val animatorRotation = ValueAnimator.ofFloat(startRotation, finalRotation).apply {
            addUpdateListener {
                ivRvDropdown.rotation = it.animatedValue as Float
            }

        }

        AnimatorSet().apply {
            duration = 200
            interpolator = LinearOutSlowInInterpolator()
            playTogether(animatorHeight, animatorRotation)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) = Unit

                override fun onAnimationEnd(animation: Animator?) = expand()

                override fun onAnimationCancel(animation: Animator?) = Unit

                override fun onAnimationStart(animation: Animator?) = Unit
            })
        }.start()

    }*/

    /* private fun refreshLayoutsIn() {
         if (isExpanded) {
             val initialHeight = container_expanded_RecyclerView.measuredHeight
             val newHeight = root_custom_expandedRecyclerView.measuredHeight
             if (initialHeight != newHeight) {
                 animateLayoutIn(newHeight, initialHeight, 180f, 0f)
             }
         }
     }*/

    /* private fun refreshLayout() {
        if (isExpanded) {
            root_custom_expandedRecyclerView.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

            root_custom_expandedRecyclerView.post {
                root_custom_expandedRecyclerView.requestLayout()
                root_custom_expandedRecyclerView.invalidate()
            }
        }
    }

    private fun refreshView() {
        if (isExpanded) {
            root_custom_expandedRecyclerView.measure(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val initialHeight = container_expanded_RecyclerView.measuredHeight
            val newHeight = root_custom_expandedRecyclerView.measuredHeight

            if (initialHeight != newHeight) {
                root_custom_expandedRecyclerView.layoutParams.height = newHeight
                root_custom_expandedRecyclerView.requestLayout()
                root_custom_expandedRecyclerView.invalidate()
            }
        }
    }*/
}