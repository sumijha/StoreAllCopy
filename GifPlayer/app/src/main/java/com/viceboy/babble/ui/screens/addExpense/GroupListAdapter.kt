package com.viceboy.babble.ui.screens.addExpense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemNewExpenseGroupListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.dataModel.Groups

class GroupListAdapter(private val callback: (Groups) -> Unit) :
    DataBoundListAdapter<Groups, ItemNewExpenseGroupListBinding>(
        diffCallback = object : DiffUtil.ItemCallback<Groups>() {
            override fun areItemsTheSame(oldItem: Groups, newItem: Groups): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Groups, newItem: Groups): Boolean {
                return oldItem.groupName == newItem.groupName && oldItem.groupDescription == newItem.groupDescription
                        && oldItem.groupMembers == newItem.groupMembers
            }

        }
    ) {

    override fun inflateBinding(viewGroup: ViewGroup): ItemNewExpenseGroupListBinding {
        val binding =
            DataBindingUtil.inflate<ItemNewExpenseGroupListBinding>(
                LayoutInflater.from(viewGroup.context),
                R.layout.item_new_expense_group_list,
                viewGroup,
                false
            )
        binding.rootGroupListItem.setOnClickListener {
            binding.groups.let {
                it?.let { callback.invoke(it) }
            }
        }
        return binding
    }

    override fun bind(binding: ItemNewExpenseGroupListBinding, item: Groups) {
        binding.groups = item
    }

}