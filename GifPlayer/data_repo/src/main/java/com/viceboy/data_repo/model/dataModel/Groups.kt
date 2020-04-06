package com.viceboy.data_repo.model.dataModel

import android.os.Parcelable
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class Groups(
    val id: String,
    val groupName: String,
    val groupDescription: String,
    val currency: String,
    val currencySymbol: String,
    val groupAdmin: LinkedTreeMap<String, LinkedTreeMap<String, Any>>,
    val groupMembers: LinkedTreeMap<String, LinkedTreeMap<String, Any>>
) : Parcelable, Serializable