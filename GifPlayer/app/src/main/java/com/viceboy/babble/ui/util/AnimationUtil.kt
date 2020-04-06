package com.viceboy.babble.ui.util

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

object AnimationUtil {

    fun rotateViewInAntiClockwiseDir(v: View, startDelay: Long? = null) {
        val delay = startDelay ?: DEFAULT_DELAY
        val orientation = v.rotation - 180f
        val objAnimator = ObjectAnimator.ofFloat(v, View.ROTATION, orientation).apply {
            duration = ANIM_SHORT_DURATION
            doOnStart { v.isClickable = false }
            doOnEnd { v.isClickable = true }
        }
        objAnimator.startDelay = delay
        objAnimator.start()
    }

    fun hideViewByScale(v: View, startDelay: Long? = null) {
        val propertyAnimator =
            v.animate().setStartDelay(startDelay ?: DEFAULT_DELAY).setDuration(ANIM_SHORT_DURATION)
                .scaleX(0f).scaleY(0f)

                .withEndAction {
                    v.visibility = View.GONE
                }
        propertyAnimator.start()
    }

    fun showViewByScale(v: View, startDelay: Long? = null) {
        v.animate().setStartDelay(startDelay ?: DEFAULT_DELAY).setDuration(ANIM_SHORT_DURATION)
            .scaleX(1f).scaleY(1f).withEndAction {
                v.visibility = View.VISIBLE
            }
    }

    private const val DEFAULT_DELAY = 0L
    private const val ANIM_SHORT_DURATION = 300L
    private const val ANIM_LONG_DURATION = 1000L
}