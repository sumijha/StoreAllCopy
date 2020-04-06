package com.viceboy.babble.ui.screens.groupDetails

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.DataBoundListAdapter
import kotlinx.android.synthetic.main.layout_group_tab_details_recycler_list.view.*

class GroupDetailsTabsAdapter(private val listOfAdapter: List<DataBoundListAdapter<out Parcelable, out ViewDataBinding>>) :
    RecyclerView.Adapter<GroupDetailsTabsAdapter.GroupTabViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupTabViewHolder =
        GroupTabViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_group_tab_details_recycler_list, parent, false)
        )

    override fun getItemCount(): Int = listOfAdapter.size

    override fun onBindViewHolder(holder: GroupTabViewHolder, position: Int) {
        holder.bindAdapter(listOfAdapter[position])
    }

    class GroupTabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindAdapter(adapter: DataBoundListAdapter<out Parcelable, out ViewDataBinding>) {
            itemView.rvGroupTabsRecyclerList.addItemDecoration(
                DividerItemDecoration(
                    itemView.context,
                    VERTICAL
                )
            )
            itemView.rvGroupTabsRecyclerList.apply {
                recycledViewPool.clear()
                adapter.notifyDataSetChanged()
                this.adapter = adapter
            }
            itemView.rvGroupTabsRecyclerList.adapter = adapter
        }
    }
}