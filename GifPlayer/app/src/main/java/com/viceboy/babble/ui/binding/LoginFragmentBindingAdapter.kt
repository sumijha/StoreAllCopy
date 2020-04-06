package com.viceboy.babble.ui.binding

import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.viceboy.babble.ui.util.animateLogo
import com.viceboy.babble.ui.util.showViewWithAlphaAnimation

@SuppressWarnings()
object LoginFragmentBindingAdapter {

    @JvmStatic
    @BindingAdapter("animateVisibility")
    fun setAnimatedVisibility(view: View, flag: Boolean) {
        if (flag)
            Handler().postDelayed({
                view.showViewWithAlphaAnimation()
            }, 300)
    }

    @JvmStatic
    @BindingAdapter("animateLogo")
    fun setAnimatedLogo(view: ImageView, flag: Boolean) {
        if (flag)
            view.animateLogo()
    }

}
