package com.viceboy.babble.ui.binding

import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.BindingAdapter
import com.viceboy.babble.ui.custom.widgets.CustomExpandableRecyclerView

object AddExpenseBinding {

    @JvmStatic
    @BindingAdapter("customBg")
    fun setCustomBackground(view: View, drawable: Drawable) {
        view.background = drawable
    }

    @JvmStatic
    @BindingAdapter("headerTextRv")
    fun setCustomTextToCollapsedView(view: CustomExpandableRecyclerView, text: String) {
        view.setHeaderText(text)
    }
}