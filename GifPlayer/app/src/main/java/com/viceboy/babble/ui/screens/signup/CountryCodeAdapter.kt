package com.viceboy.babble.ui.screens.signup

import android.content.Context
import com.viceboy.babble.ui.base.DataBoundArrayAdapter


class CountryCodeAdapter(
    context: Context,
    layoutRes:Int,
    values:ArrayList<String>
) : DataBoundArrayAdapter<String>(context,layoutRes,values) {
    override fun performFiltering(value: String, queryString: String): Boolean = value.contains(queryString)

    override fun getValueFromList(dataObject: String?): CharSequence? = dataObject

}