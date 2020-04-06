package com.viceboy.babble.ui.screens.addExpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemNewExpenseAmountPaidByListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.dataModel.User

class ExpenseOwnerListAdapter(
    private var adminUserId: String,
    private val callback: (User) -> Unit
) :
    DataBoundListAdapter<User, ItemNewExpenseAmountPaidByListBinding>(
        diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = false

        }
    ) {

    override fun inflateBinding(viewGroup: ViewGroup): ItemNewExpenseAmountPaidByListBinding {
        val binding =
            DataBindingUtil.inflate<ItemNewExpenseAmountPaidByListBinding>(
                LayoutInflater.from(viewGroup.context),
                R.layout.item_new_expense_amount_paid_by_list,
                viewGroup,
                false
            )
        binding.rootExpenseOwnerListItem.setOnClickListener {
            binding.user.let {
                it?.let { callback.invoke(it) }
            }
        }
        return binding
    }

    override fun bind(binding: ItemNewExpenseAmountPaidByListBinding, item: User) {
        binding.user = item
        if (item.id == adminUserId)
            binding.tvAdminTag.visibility = View.VISIBLE
        else
            binding.tvAdminTag.visibility = View.GONE
    }

    fun setAdminUserId(id:String) {
        adminUserId = id
    }

}