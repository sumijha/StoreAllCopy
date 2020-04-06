package com.viceboy.babble.ui.binding

import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.ITALIC
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.databinding.BindingAdapter
import com.google.android.material.appbar.AppBarLayout
import com.viceboy.babble.R

object DashboardFragmentBinding {

    @JvmStatic
    @BindingAdapter("animatorType", "animeFab")
    fun setFabAnimator(view: View, animatorType: String, animeFab: Boolean) {
        val animFabOpen = AnimationUtils.loadAnimation(view.context, R.anim.fab_menu_open)
        val animFabClose = AnimationUtils.loadAnimation(view.context, R.anim.fab_menu_close)
        val animFabRotate = AnimationUtils.loadAnimation(view.context, R.anim.fab_rotate_clock)
        val animFabRotateAnti =
            AnimationUtils.loadAnimation(view.context, R.anim.fab_rotate_anticlock)

        if (animeFab)
            when (animatorType) {
                "rotateClock" -> view.post { view.startAnimation(animFabRotate) }

                "rotateAntiClock" -> view.post { view.startAnimation(animFabRotateAnti) }

                "open" -> {
                    view.visibility = View.VISIBLE
                    view.startAnimation(animFabOpen)
                }

                "close" -> {
                    view.visibility = View.INVISIBLE
                    view.startAnimation(animFabClose)
                }
            }
    }

    @JvmStatic
    @BindingAdapter("onMenuItemListener")
    fun setMenuItemListener(view: View, listener: OnMenuItemClickListener) {
        (view as Toolbar).setOnMenuItemClickListener(listener)
    }

    @JvmStatic
    @BindingAdapter("offSetChangeListener")
    fun setAppBarOffsetChangeListener(view: View, listener: AppBarLayout.OnOffsetChangedListener) {
        (view as AppBarLayout).addOnOffsetChangedListener(listener)
    }

    @JvmStatic
    @BindingAdapter("onFabMenuOpenedTouchListener")
    fun setFabMenuTouchListener(view: View, listener: View.OnTouchListener) {
        view.setOnTouchListener(listener)
    }

    @JvmStatic
    @BindingAdapter("isSelectable")
    fun setSelected(view: View, flag: Boolean) {
        view.isSelected = flag
    }

    @JvmStatic
    @BindingAdapter("groupSpanText")
    fun setSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                StyleSpan(BOLD),
                inputText.indexOf("("),
                inputText.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                inputText.indexOf("("),
                inputText.length,
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }

    @JvmStatic
    @BindingAdapter("expenseSpanText")
    fun setExpenseSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                StyleSpan(BOLD),
                0,
                inputText.indexOf(":"),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(ITALIC),
                inputText.indexOf(":") + 1,
                inputText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                inputText.indexOf(":") + 1,
                inputText.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }

    @JvmStatic
    @BindingAdapter("memberTabSpanText")
    fun setMemberSpannableText(view: TextView, inputText: String) {
        val transformText = inputText.replace("[", "")
            .replace("]", "")
        val spannable = SpannableStringBuilder(transformText)
        spannable.apply {
            setSpan(
                StyleSpan(BOLD),
                0,
                transformText.indexOf(":"),
                Spannable.SPAN_EXCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }

    @JvmStatic
    @BindingAdapter("nullSpanText")
    fun setNullSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(BOLD),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                RelativeSizeSpan(1.12f),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.SANS_SERIF.style),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(ITALIC),
                inputText.indexOf("\n"),
                inputText.indexOf("+") - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                StyleSpan(ITALIC),
                inputText.indexOf("+") + 1,
                inputText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimaryDark)),
                inputText.indexOf("+") - 1,
                inputText.indexOf("+") + 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(BOLD),
                inputText.indexOf("+") - 1,
                inputText.indexOf("+") + 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                RelativeSizeSpan(1.2f),
                inputText.indexOf("+") - 1,
                inputText.indexOf("+") + 1,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }
        view.text = spannable
    }

    @JvmStatic
    @BindingAdapter("placeholderSpanText")
    fun setPlaceholderSpannableText(view: TextView, inputText: String) {
        val spannable = SpannableStringBuilder(inputText)
        spannable.apply {
            setSpan(
                ForegroundColorSpan(view.context.getColor(R.color.colorPrimary)),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(BOLD),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                RelativeSizeSpan(1.12f),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(Typeface.SANS_SERIF.style),
                0,
                inputText.indexOf("\n"),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            setSpan(
                StyleSpan(ITALIC),
                inputText.indexOf("\n"),
                inputText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        view.text = spannable
    }
}