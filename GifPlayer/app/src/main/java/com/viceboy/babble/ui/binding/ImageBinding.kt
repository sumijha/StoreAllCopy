package com.viceboy.babble.ui.binding

import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.viceboy.babble.R

object ImageBinding {

    @JvmStatic
    @BindingAdapter("imageUrl", "imageListener", requireAll = false)
    fun setImageFromUri(imageView: View, uri: Uri?, listener: RequestListener<Drawable>?) {
        uri?.let {
            Glide.with(imageView.context)
                .load(it)
                .listener(listener)
                .apply(RequestOptions().centerInside())
                .into(imageView as ImageView)
        }
    }

    @JvmStatic
    @BindingAdapter("imageFromUrl", "imageListener", requireAll = false)
    fun setImageFromUrl(imageView: View, url: String?, listener: RequestListener<Drawable>?) {
        url?.let {
            Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.bg_user_profile_placeholder)
                .listener(listener)
                .apply(RequestOptions().centerInside())
                .into(imageView as ImageView)
        }
    }

    @JvmStatic
    @BindingAdapter("imageFromShopUrl", "imageListener", requireAll = false)
    fun setImageFromExpenseUrl(imageView: View, url: String?, listener: RequestListener<Drawable>?) {
        url?.let {
            Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.junk_test_image_shopping)
                .listener(listener)
                .apply(RequestOptions().centerInside())
                .into(imageView as ImageView)
        }
    }
}