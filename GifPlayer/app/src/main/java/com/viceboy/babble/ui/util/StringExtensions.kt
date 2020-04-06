package com.viceboy.babble.ui.util

import android.annotation.SuppressLint
import android.text.Editable
import android.util.Patterns
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

fun String.isEmailPattern(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isMobileNoPattern(): Boolean = Patterns.PHONE.matcher(this).matches() && this.length > 4

fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

@SuppressLint("SimpleDateFormat")
fun String.toDate(format:String): Date? = SimpleDateFormat(format).parse(this)
