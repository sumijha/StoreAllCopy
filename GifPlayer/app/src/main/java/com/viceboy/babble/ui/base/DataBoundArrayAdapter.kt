package com.viceboy.babble.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

@Suppress("UNCHECKED_CAST")
abstract class DataBoundArrayAdapter<T>(
    context: Context,
    private val layoutRes: Int,
    private var allValues: ArrayList<T>
) : ArrayAdapter<T>(context, layoutRes, allValues), Filterable {

    private var mListOfValues = allValues

    override fun getCount(): Int = mListOfValues.size

    override fun getItem(position: Int): T? {
        return mListOfValues[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView as TextView? ?: LayoutInflater.from(parent.context).inflate(
            layoutRes,
            parent,
            false
        ) as TextView
        view.text = getValueFromList(mListOfValues[position])
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val queryString = constraint.toString()
                val filterResults = FilterResults()
                filterResults.values = if (queryString.isEmpty()) {
                    allValues
                } else {
                    allValues.filter {
                         performFiltering(it,queryString)
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mListOfValues = results?.values as ArrayList<T>
                notifyDataSetChanged()
            }
        }
    }

    abstract fun performFiltering(value:T,queryString: String):Boolean

    abstract fun getValueFromList(dataObject: T?): CharSequence?
}