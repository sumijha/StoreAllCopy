package com.viceboy.babble.ui.binding

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.viceboy.babble.R

object GroupDetailBinding {

    @JvmStatic
    @BindingAdapter("bottomFromSpanText")
    fun setGroupTopSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                inputText.indexOf("should"),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                inputText.indexOf("should"),
                inputText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                RelativeSizeSpan(1.12f),
                0,
                inputText.indexOf("should"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }

    @JvmStatic
    @BindingAdapter("bottomToSpanText")
    fun setGroupBottomSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                0,
                inputText.indexOf("to")+3,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                RelativeSizeSpan(1.12f),
                inputText.indexOf("to")+3,
                inputText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.BOLD),
                inputText.indexOf("to")+3,
                inputText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }
}