package com.viceboy.babble.ui.binding

import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.databinding.BindingAdapter
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.google.android.material.textfield.TextInputEditText
import com.viceboy.babble.ui.screens.signup.CountryCodeAdapter

object SignUpFragmentBindingAdapter {

    @JvmStatic
    @BindingAdapter("isVisibleFlag")
    fun animateVisibility(view: View, flag: Boolean) {
        if (flag) {
            TransitionManager.beginDelayedTransition(
                view.parent as ViewGroup,
                Fade().apply {
                    startDelay = 200
                    duration = 500
                    interpolator = LinearOutSlowInInterpolator()
                }
            )
            view.visibility = View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("setAdapter")
    fun setAutoCompleteTextViewAdapter(view: AutoCompleteTextView, arrayCodes: ArrayList<String>) {
        val countryAdapter = CountryCodeAdapter(
            view.context!!, android.R.layout.simple_list_item_1,
            arrayCodes
        )
        view.setAdapter(countryAdapter)
    }

    @JvmStatic
    @BindingAdapter("errorInCountryCode")
    fun setErrorFlagForInvalidCountryCode(view: TextInputEditText, flag: Boolean) {
        if (flag)
            view.error = "Invalid Country Code Entered"
        else
            view.error = null
    }
}