package com.viceboy.babble.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

abstract class BaseBottomSheetFragment<T : ViewDataBinding, V : ViewModel> :
    BottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected abstract val layoutRes: Int
    protected abstract val viewModelClass: Class<V>
    protected var binding: T? = null
    protected val viewModel: V by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(viewModelClass)
    }

    protected abstract fun onCreateView(binding: T): Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate<T>(
            inflater,
            layoutRes,
            container,
            false
        )
        onCreateView(binding as T)
        return (binding as T).root
    }
}