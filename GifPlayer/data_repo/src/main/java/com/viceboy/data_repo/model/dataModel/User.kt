package com.viceboy.data_repo.model.dataModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class User(
    val id: String,
    val email: String,
    val countryCode: String,
    val country: String,
    val name: String,
    var phone: String,
    val avatar_url: String?,
    val groups: List<String>?
) : Parcelable, Serializable,
    ForExpenseShared {

    override fun equals(other: Any?): Boolean {
        other?:return false
        if (other !is User) return false
        return this.id == other.id
    }

    override fun hashCode(): Int = this.id.length+this.email.length+this.name.length
}

data class TotalExpenseShared(val id: String="") :
    ForExpenseShared

interface ForExpenseShared