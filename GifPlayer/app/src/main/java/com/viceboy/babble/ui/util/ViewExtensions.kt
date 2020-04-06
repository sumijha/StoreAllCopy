package com.viceboy.babble.ui.util

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.postDelayed
import androidx.core.widget.NestedScrollView
import androidx.transition.TransitionManager
import com.viceboy.babble.R


fun View.toggleVisibility(onVisible:Int.()->Unit) {
    this.postDelayed(100) {
        val visibility = if (this.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        val viewGroup = this.rootView as ViewGroup
        TransitionManager.beginDelayedTransition(viewGroup)
        this.visibility = visibility
        onVisible(visibility)
    }
}

fun View.animateCircularReveal(
    animationDuration: Long,
    animatorListener: Animator.AnimatorListener?
) {
    val width = this.width
    val height = this.height
    val centerX = width / 2
    val centerY = height / 2
    val radius = width.coerceAtLeast(height).toFloat()
    ViewAnimationUtils.createCircularReveal(this, centerX, centerY, radius, 0f).apply {
        duration = animationDuration
        addListener(animatorListener)
        start()
    }
}

fun View.showViewWithAlphaAnimation() = also { view ->
    view.visibility = View.VISIBLE
    val animatorSet = AnimatorSet()
    animatorSet.playTogether(
        ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, -(view.height / 2).toFloat(), 0f),
        ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
    )
    animatorSet.apply {
        interpolator = DecelerateInterpolator()
        duration = ALPHA_DURATION_LONG
    }
    animatorSet.start()
}

fun ImageView.animateLogo() = also { view ->
    val height = resources.getDimension(R.dimen.login_logo_height)
    val width = resources.getDimension(R.dimen.login_logo_width)

    val animatorHeight = valueAnimationWithInt(5, height.toInt()) {
        view.apply {
            layoutParams.height = it.animatedValue as Int
            requestLayout()
        }
    }

    val animatorWidth = valueAnimationWithInt(2, width.toInt()) {
        view.apply {
            layoutParams.width = it.animatedValue as Int
            requestLayout()
        }
    }

    val animatorRotate = valueAnimationWithFloat(-90f, 0f) {
        view.rotation = it.animatedValue as Float
    }

    AnimatorSet().apply {
        duration = 800
        interpolator = BounceInterpolator()
        playTogether(animatorHeight, animatorWidth, animatorRotate)
        start()
    }
}

fun View.animateButtonClick(animatorListener: Animator.AnimatorListener?) = also { view ->
    val textSize = (view as Button).textSizeInDP()
    val buttonWidth = view.width

    val animatorWidth = valueAnimationWithInt(view.width, 0) {
        view.apply {
            layoutParams.width = it.animatedValue as Int
            requestLayout()
        }
    }

    val animatorPosX = valueAnimationWithFloat(0f, view.width / 2.toFloat()) {
        view.apply {
            x = it.animatedValue as Float
            requestLayout()
        }
    }

    val animatorText = valueAnimationWithFloat(textSize, 0f) {
        val currentWidth = view.width
        if (currentWidth < buttonWidth / 2)
            view.textSize = it.animatedValue as Float
        view.requestLayout()
    }

    AnimatorSet().apply {
        duration = 500
        addListener(animatorListener)
        playTogether(animatorWidth, animatorPosX, animatorText)
        start()
    }
}

fun NestedScrollView.scrollToTop() {
    this.smoothScrollTo(0, 0)
}

fun View.locateView(): Rect {
    val arrLoc = IntArray(2)
    this.getLocationOnScreen(arrLoc)
    val location = Rect()
    location.left = arrLoc[0]
    location.top = arrLoc[1]
    location.right = location.left + this.width
    location.bottom = location.top + this.height

    return location
}

fun EditText.vibrateText(onStart: Animator.() -> Unit, onEnd: Animator.() -> Unit) {
    val initialPos = if (this.compoundDrawablePadding > 0)
        this.compoundDrawablePadding
    else
        this.paddingStart

    val fieldWidth = this.width
    val widthOccupiedByText = this.getWidthOccupiedByText().plus(initialPos)

    if (widthOccupiedByText < fieldWidth) {
        val finalPos = (fieldWidth - widthOccupiedByText) * 0.4.toInt()
        val valueAnimation = valueAnimationWithInt(initialPos, finalPos) {
            this.compoundDrawablePadding = it.animatedValue as Int
        }

        (valueAnimation as ValueAnimator).repeatMode = ValueAnimator.REVERSE
        valueAnimation.repeatCount = 4

        valueAnimation.apply {
            duration = 50
            interpolator = AnticipateOvershootInterpolator()
            doOnStart { onStart(it) }
            doOnEnd {
                this@vibrateText.compoundDrawablePadding = initialPos
                onEnd(it)
            }
            start()
        }
    }
}

private fun EditText.getWidthOccupiedByText(): Int {
    this.measure(0, 0)
    return this.measuredWidth
}

private fun Button.textSizeInDP() = this.textSize / resources.displayMetrics.scaledDensity

private fun valueAnimationWithFloat(
    startRange: Float,
    endRange: Float,
    updateListener: (ValueAnimator) -> Unit
): Animator? {
    return ValueAnimator.ofFloat(startRange, endRange).apply {
        addUpdateListener {
            updateListener.invoke(it)
        }
    }
}

private fun valueAnimationWithInt(
    startRange: Int,
    endRange: Int,
    updateListener: (ValueAnimator) -> Unit
): Animator? {
    return ValueAnimator.ofInt(startRange, endRange).apply {
        addUpdateListener {
            updateListener.invoke(it)
        }
    }
}

private const val ALPHA_DURATION_LONG: Long = 400
