package com.viceboy.babble.ui.custom.widgets

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 * Creating CustomDashboardLinearLayoutManager in order to handle recycler item index out of bounds exception
 */
class CustomDashboardLinearLayoutManager(
    context: Context,
    private val onDecoratedMeasuredHeight: Int.() -> Unit
) : LinearLayoutManager(context) {
    override fun getDecoratedMeasuredHeight(child: View): Int {
        val itemHeight = super.getDecoratedMeasuredHeight(child)
        onDecoratedMeasuredHeight.invoke(itemHeight)
        return itemHeight
    }

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Timber.e("Exception caught in Layout children")
        } catch (ie : IllegalArgumentException) {
            Timber.e("IllegalArgumentException caught in Layout children")
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean = false
}