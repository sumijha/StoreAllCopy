package com.viceboy.babble.ui.screens.groupDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemGroupDetailsMemberListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.uiModel.GroupMembers

class MemberDetailsAdapter : DataBoundListAdapter<GroupMembers,ItemGroupDetailsMemberListBinding>(
    diffCallback = object : DiffUtil.ItemCallback<GroupMembers>() {
        override fun areItemsTheSame(oldItem: GroupMembers, newItem: GroupMembers): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GroupMembers, newItem: GroupMembers): Boolean =
            oldItem.amountPaid == newItem.amountPaid && oldItem.avatarUrl == newItem.avatarUrl
    }
) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemGroupDetailsMemberListBinding {
        return DataBindingUtil.inflate(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_group_details_member_list,
            viewGroup,
            false
        )
    }

    override fun bind(binding: ItemGroupDetailsMemberListBinding, item: GroupMembers) {
        binding.members = item
    }

}