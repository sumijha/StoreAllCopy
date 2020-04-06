package com.viceboy.babble.ui.screens.groupDetails.dialog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemSettleUpListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.uiModel.SettlementPair

class GroupSettlementListAdapter(
    private val fm: FragmentManager,
    private val onPayClick: SettlementPair.() -> Unit
) : DataBoundListAdapter<SettlementPair, ItemSettleUpListBinding>(
    diffCallback = object : DiffUtil.ItemCallback<SettlementPair>() {
        override fun areItemsTheSame(oldItem: SettlementPair, newItem: SettlementPair): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SettlementPair, newItem: SettlementPair): Boolean =
            oldItem.fromUser == newItem.fromUser && oldItem.amount == newItem.amount

    }
) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemSettleUpListBinding {
        val binding = DataBindingUtil.inflate<ItemSettleUpListBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_settle_up_list,
            viewGroup,
            false
        )

        return binding
    }

    override fun bind(binding: ItemSettleUpListBinding, item: SettlementPair) {
        binding.pair = item
        binding.btnPay.setOnClickListener {
            onPayClick(item)
            GroupSettlementConfirmBottomSheetFragment.newInstance().show(fm,"Dialog")
        }
    }

}