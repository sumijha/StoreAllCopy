package com.viceboy.babble.ui.screens.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemDashboardMemberTabBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.uiModel.DashboardMembers

class DashboardMemberTabListAdapter(private val callback: DashboardMembers.() -> Unit) :
    DataBoundListAdapter<DashboardMembers, ItemDashboardMemberTabBinding>(
        diffCallback = object : DiffUtil.ItemCallback<DashboardMembers>() {
            override fun areItemsTheSame(
                oldItem: DashboardMembers,
                newItem: DashboardMembers
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: DashboardMembers,
                newItem: DashboardMembers
            ): Boolean = oldItem.name == newItem.name

        }
    ) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemDashboardMemberTabBinding {
        val binding = DataBindingUtil.inflate<ItemDashboardMemberTabBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_dashboard_member_tab,
            viewGroup,
            false
        )
        binding.root.setOnClickListener {
            binding.dashboardMembers?.let { callback.invoke(it) }
        }
        return binding
    }

    override fun bind(binding: ItemDashboardMemberTabBinding, item: DashboardMembers) {
        binding.dashboardMembers = item
    }
}