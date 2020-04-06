package com.viceboy.data_repo.model.uiModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DashboardMembers(
    val id : String,
    val name : String,
    var avatarImage : String?,
    var inGroups : List<String>?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is DashboardMembers) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.name.length
}