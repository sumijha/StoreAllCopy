package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupMembers(
    val id:String,
    var name : String,
    var isAdmin : Boolean?,
    var currencySymbol : String?,
    var currency : String?,
    var avatarUrl : String?,
    var amountPaid : Float
)  :Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is GroupMembers) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.amountPaid.toInt() + this.name.length
}