package com.viceboy.babble.ui.screens.groupDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemGroupDetailsPaymentListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.uiModel.GroupTransactions


class PaymentDetailsAdapter :
    DataBoundListAdapter<GroupTransactions, ItemGroupDetailsPaymentListBinding>(
        diffCallback = object : DiffUtil.ItemCallback<GroupTransactions>() {
            override fun areItemsTheSame(oldItem: GroupTransactions, newItem: GroupTransactions): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: GroupTransactions, newItem: GroupTransactions): Boolean =
                oldItem.groupId == newItem.groupId
        }
    ) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemGroupDetailsPaymentListBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_group_details_payment_list,
            viewGroup,
            false
        )
    }

    override fun bind(binding: ItemGroupDetailsPaymentListBinding, item: GroupTransactions) {
        binding.transaction = item
    }


}
