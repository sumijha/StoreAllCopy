package com.viceboy.data_repo.model.dataModel

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable


@Parcelize
data class Expense(
    val id: String,
    val itemName: String,
    val expenseImageUrl: String?,
    val expenseDate: Long,
    val amountPaid: Float,
    val currency: String,
    val groupId: String,
    val expenseOwner: String,
    val expenseSharedBy: HashMap<String, Float>
) : Parcelable, Serializable {
    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (other !is Expense) return false
        return this.id == other.id
    }

    override fun hashCode(): Int =
        this.id.length + this.itemName.length + this.expenseDate.toString().length
}