package com.viceboy.babble.ui.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemDashboardGroupsTabBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.uiModel.DashboardGroup

class DashboardGroupTabListAdapter(private val onCallback: DashboardGroup.() -> Unit) :
    DataBoundListAdapter<DashboardGroup, ItemDashboardGroupsTabBinding>(
        diffCallback = object : DiffUtil.ItemCallback<DashboardGroup>() {
            override fun areItemsTheSame(
                oldItem: DashboardGroup,
                newItem: DashboardGroup
            ): Boolean =
                oldItem.groupId == newItem.groupId

            override fun areContentsTheSame(
                oldItem: DashboardGroup,
                newItem: DashboardGroup
            ): Boolean =
                oldItem.groupName == newItem.groupName

        }
    ) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemDashboardGroupsTabBinding {
        val binding = DataBindingUtil.inflate<ItemDashboardGroupsTabBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_dashboard_groups_tab,
            viewGroup,
            false
        )

        binding.root.setOnClickListener { binding.dashGroup?.let { onCallback(it) } }
        return binding
    }

    override fun bind(binding: ItemDashboardGroupsTabBinding, item: DashboardGroup) {
        binding.dashGroup = item
    }
}