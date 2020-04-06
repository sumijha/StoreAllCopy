package com.viceboy.babble.ui.binding

import android.animation.Animator
import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.viceboy.babble.ui.util.AnimationUtil
import com.viceboy.babble.ui.util.animateButtonClick
import com.viceboy.babble.ui.util.isEmailPattern
import com.viceboy.babble.ui.util.isMobileNoPattern

object CommonBinding {

    @JvmStatic
    @BindingAdapter("onClickWithValidFields", "buttonAnimationListener")
    fun setActionOnSignUpClickWithValidFields(
        view: View,
        flag: Boolean,
        animatorListener: Animator.AnimatorListener
    ) {
        if (flag)
            view.animateButtonClick(animatorListener)
    }

    @JvmStatic
    @BindingAdapter("showViewWithAnimation")
    fun setUpScaleVisibility(view: View, flag: Boolean) {
        if (flag)
            view.post {
                AnimationUtil.showViewByScale(view)
            }
    }


    @JvmStatic
    @BindingAdapter("fieldType", "fieldName", "checkFlag", requireAll = false)
    fun setFieldValidation(
        view: View,
        fieldType: String,
        fieldName: String,
        checkFlag: Boolean
    ) {
        checkFlag.let {
            if (it && fieldType.contentEquals("required")) {
                val inputValue = (view as TextInputEditText).text.toString()
                val errorText = if (inputValue.isEmpty()) {
                    "$fieldName is a required field"
                } else {
                    when (fieldName) {
                        "Name" -> {
                            if (inputValue.length < 3)
                                "Invalid Name entered"
                            else null
                        }

                        "Mobile Number" -> {
                            if (!inputValue.isMobileNoPattern())
                                "Invalid Mobile Number entered"
                            else null
                        }

                        "Email Or Phone" -> {
                            if (inputValue.isEmailPattern() || inputValue.isMobileNoPattern())
                                null
                            else "Invalid Email Address or Mobile No. entered"
                        }

                        "Email" -> {
                            if (!inputValue.isEmailPattern())
                                "Invalid Email Address entered"
                            else null
                        }

                        "Password" -> {
                            if (inputValue.length < 5)
                                "Password length should be greater than 5 digits"
                            else null
                        }
                        else -> null
                    }
                }
                errorText.let {
                    view.error = it
                }
            }
        }
    }
}