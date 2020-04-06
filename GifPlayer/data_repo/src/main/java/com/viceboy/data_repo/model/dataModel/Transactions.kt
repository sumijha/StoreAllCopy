package com.viceboy.data_repo.model.dataModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Transactions(
    val id: String,
    val paymentDate: Long,
    val amount: Float,
    val currencySymbol: String,
    val groupId: String,
    val paidBy: String,
    val paidTo: String
) : Parcelable,Serializable