package com.viceboy.data_repo.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DataUtils {
    fun getDateFromMilliSec(timeInMilli: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            sdf.format(Date(timeInMilli))
        } catch (dfe: ParseException) {
            ""
        }
    }

    fun getCurrentYear() = Calendar.getInstance().get(Calendar.YEAR)
}