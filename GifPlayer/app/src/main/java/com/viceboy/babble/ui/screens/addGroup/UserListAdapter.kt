package com.viceboy.babble.ui.screens.addGroup

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.viceboy.babble.R
import com.viceboy.babble.databinding.ItemNewGroupParticipantsListBinding
import com.viceboy.babble.ui.base.DataBoundListAdapter
import com.viceboy.data_repo.model.dataModel.User

class UserListAdapter(private val trashClickCallback: User.() -> Unit) :
    DataBoundListAdapter<User, ItemNewGroupParticipantsListBinding>(
        diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem.name == newItem.name && oldItem.email == newItem.email
        }
    ) {
    override fun inflateBinding(viewGroup: ViewGroup): ItemNewGroupParticipantsListBinding {
        val binding = DataBindingUtil.inflate<ItemNewGroupParticipantsListBinding>(
            LayoutInflater.from(viewGroup.context),
            R.layout.item_new_group_participants_list,
            viewGroup,
            false
        )
        binding.ibRemoveParticipant.setOnClickListener {
            binding.user?.let {
                trashClickCallback.invoke(it)
            }
        }
        return binding
    }

    override fun bind(binding: ItemNewGroupParticipantsListBinding, item: User) {
        binding.user = item
    }
}