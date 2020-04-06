package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupExpense(
    val id: String,
    val itemName: String,
    val itemImage: String,
    val itemAmount: Float,
    var group: String,
    var currency: String,
    var currencySymbol : String,
    val expenseDate: String,
    var expenseOwner: String,
    var expensePaidTo: MutableMap<String, Float>
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is GroupExpense) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.itemName.length + this.expenseDate.length + this.itemImage.length
}