package com.viceboy.babble.ui.custom.behavior

import android.content.Context
import android.view.View
import android.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.viceboy.babble.R
import de.hdodenhof.circleimageview.CircleImageView

class CustomAvatarSwipeBehavior(context: Context) :
    CoordinatorLayout.Behavior<CircleImageView>() {

    private val mStartHeight = context.resources.getDimension(R.dimen.avatar_initial_height)
    private val mStartWidth = context.resources.getDimension(R.dimen.avatar_initial_width)
    private val mFinalHeight = context.resources.getDimension(R.dimen.avatar_final_height)
    private val mFinalToolbarColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val mStartToolbarColor = ContextCompat.getColor(context, android.R.color.transparent)
    private var mStartPositionY: Int = 0
    private var mFinalPositionY: Int = 0
    private var mStartChildPositionX: Float = 0f
    private var mStartChildPositionY: Float = 0f
    private var mStartToolBarPosition: Float = 0f

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        return dependency is Toolbar
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        modifyAvatarDependingState(child, dependency)
        return true
    }

    private fun modifyAvatarDependingState(child: CircleImageView, dependency: View) {
        initBaseProperties(child, dependency)

        val lp = child.layoutParams
        val maxScrollDistance = mStartToolBarPosition
        val expansionFactor = dependency.y / maxScrollDistance

        if (expansionFactor < MIN_AVATAR_PERCENTAGE_SIZE) {
            val distanceYToSubtract: Float = ((mStartPositionY - mFinalPositionY)
                    * (1f - expansionFactor)) + child.height / 2
            val heightFactor = MIN_AVATAR_PERCENTAGE_SIZE - expansionFactor
            val heightToSubtract = (mStartHeight + 2.5 * mFinalHeight) * heightFactor

            lp.width = (mStartHeight - heightToSubtract).toInt()
            lp.height = (mStartHeight - heightToSubtract).toInt()
            child.layoutParams = lp
            child.x = mStartChildPositionX
            child.y = mStartPositionY - distanceYToSubtract
            dependency.setBackgroundColor(mFinalToolbarColor)
        } else {
            val distanceYToSubtract: Float =
                (1 + MIN_AVATAR_PERCENTAGE_SIZE / 2) * (mStartChildPositionY - expansionFactor * mStartChildPositionY)
            lp.width = (mStartWidth).toInt()
            lp.height = (mStartHeight).toInt()
            child.layoutParams = lp
            child.x = mStartChildPositionX
            child.y = mStartChildPositionY - distanceYToSubtract
            dependency.setBackgroundColor(mStartToolbarColor)
        }
    }

    private fun initBaseProperties(
        child: CircleImageView,
        dependency: View
    ) {
        if (mStartPositionY == 0)
            mStartPositionY = dependency.y.toInt()

        if (mFinalPositionY == 0)
            mFinalPositionY = dependency.height / 2

        if (mStartToolBarPosition == 0f)
            mStartToolBarPosition = dependency.y

        if (mStartChildPositionX == 0f)
            mStartChildPositionX = child.x

        if (mStartChildPositionY == 0f)
            mStartChildPositionY = child.y
    }

    companion object {
        private const val MIN_AVATAR_PERCENTAGE_SIZE = 0.3f
    }
}