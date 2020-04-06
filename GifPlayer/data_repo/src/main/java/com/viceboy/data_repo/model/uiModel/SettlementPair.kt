package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SettlementPair(
    var id: Long,
    var fromUser: String,
    var toUser: String,
    var amount: Float,
    var currency: String
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is SettlementPair) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.toString().length+this.amount.toInt()+this.fromUser.length
}