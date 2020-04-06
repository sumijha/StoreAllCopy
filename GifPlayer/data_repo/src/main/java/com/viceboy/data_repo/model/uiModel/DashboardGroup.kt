package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DashboardGroup(
    var groupId : String,
    var groupName: String,
    var groupAdmin : String,
    var groupDesc : String?,
    var groupCurrency: String,
    var currencySymbol : String,
    var owedText : String,
    var mapOfOutstandingAmountByUser : MutableMap<String,Float>?,
    var amountOwedByCurrentUser: Float,
    var lastTransactionBy: String?,
    var lastTransaction: Float?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is DashboardGroup) return false
        return this.groupId == other.groupId
    }

    override fun hashCode(): Int =
        this.groupId.length + this.owedText.length + this.groupName.length
}