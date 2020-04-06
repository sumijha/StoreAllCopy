package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DashboardExpense(
    val id: String,
    val itemName: String,
    val expenseDate: String,
    var expenseOwner: String?,
    val inCurrency: String,
    var inGroup: String?,
    var itemAmount: Float
): Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is DashboardExpense) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.itemName.length + this.expenseDate.length
}