package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExpenseInfo(
    val id: String,
    var itemName: String,
    var itemImage: String,
    var itemAmount: Float,
    var currencySymbol: String,
    var groupName: String,
    var expenseDate: String,
    var paidBy: String,
    var contributorsName: String,
    var contributorsAmount: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is ExpenseInfo) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.itemName.length + this.expenseDate.length + this.itemImage.length + this.contributorsName.length
}