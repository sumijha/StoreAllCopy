package com.viceboy.babble.ui.screens.groupDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemGroupDetailsExpenseListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.babble.ui.base.DataBoundViewHolder
import com.viceboy.data_repo.model.uiModel.GroupExpense
import timber.log.Timber


class ExpenseDetailsAdapter(private val callback: GroupExpense.() -> Unit) :
    DataBoundListAdapter<GroupExpense, ItemGroupDetailsExpenseListBinding>(
        diffCallback = object : DiffUtil.ItemCallback<GroupExpense>() {
            override fun areItemsTheSame(oldItem: GroupExpense, newItem: GroupExpense): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: GroupExpense,
                newItem: GroupExpense
            ): Boolean =
                oldItem.itemName == newItem.itemName && oldItem.itemImage == newItem.itemImage

        }
    ) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemGroupDetailsExpenseListBinding {
        val binding = DataBindingUtil.inflate<ItemGroupDetailsExpenseListBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_group_details_expense_list,
            viewGroup,
            false
        )

        binding.root.setOnClickListener {
            binding.expense?.let { callback(it) }
        }
        return binding
    }

    override fun onBindViewHolder(
        holder: DataBoundViewHolder<ItemGroupDetailsExpenseListBinding>,
        position: Int
    ) {
        Timber.e("Expense ItemCount: $itemCount")
        super.onBindViewHolder(holder, position)
    }

    override fun bind(binding: ItemGroupDetailsExpenseListBinding, item: GroupExpense) {
        binding.expense = item
    }

}
