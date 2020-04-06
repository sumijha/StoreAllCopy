package com.viceboy.babble.ui.screens.addExpense

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.viceboy.babble.ui.base.DataBoundViewHolder

abstract class BaseDataBoundAdapter<T>(diffCallBack : DiffUtil.ItemCallback<T>) : ListAdapter<T,DataBoundViewHolder<*>>(diffCallBack) {

    abstract fun inflateBinding(viewGroup: ViewGroup,viewType: Int): DataBoundViewHolder<*>

    abstract fun <V:ViewDataBinding> bind(binding: V, item: T)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<*> {
        return inflateBinding(parent,viewType)
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<*>, position: Int) {
        bind(holder.binding, getItem(position))
        holder.binding.executePendingBindings()
    }

}