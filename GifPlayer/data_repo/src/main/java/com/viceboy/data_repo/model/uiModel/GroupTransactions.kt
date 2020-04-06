package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupTransactions(
    val id: String,
    val groupId: String,
    var date: String,
    var currency: String,
    var amount: Float,
    var paidBy: String,
    var paidByImage: String,
    var paidTo: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is GroupTransactions) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.groupId.length + this.date.length
}