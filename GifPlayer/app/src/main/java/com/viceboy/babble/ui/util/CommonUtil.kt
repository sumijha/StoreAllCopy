package com.viceboy.babble.ui.util

import android.app.Activity
import android.content.Context
import java.util.*

object Constants {
    const val RC_IMAGE = 100
    const val KEY_FROM_CAPTURE_FRAGMENT = "capture_fragment"
    const val KEY_EQUAL = "equally"
    const val KEY_UNEQUAL = "unequally"
}

fun Activity.clearFocus() {
    val decorView = this.window.decorView
    if (decorView.hasFocus())
        decorView.clearFocus()
}

fun Context.statusBarHeight(): Int {
    var result = 0
    val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = resources.getDimensionPixelSize(resourceId)
    }
    return result
}

fun Calendar.getCurrentYear(): Int {
    return Calendar.getInstance().get(Calendar.YEAR)
}