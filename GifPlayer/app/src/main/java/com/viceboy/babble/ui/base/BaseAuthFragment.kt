package com.viceboy.babble.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseAuthFragment<VM : ViewModel, VB : ViewDataBinding> : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val compositeDisposable = CompositeDisposable()
    protected open lateinit var binding: VB
    protected open val viewModel: VM by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(viewModelClass)
    }

    @LayoutRes
    protected abstract fun layoutRes(): Int

    protected abstract fun onCreateView()
    protected abstract fun observeLiveData(viewModel: VM, binding: VB)
    protected abstract val viewModelClass: Class<VM>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutRes(), container, false)
        startObservingLiveData()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateView()
    }

    override fun onDestroy() {
        enableTouch()
        compositeDisposable.clear()
        super.onDestroy()
    }

    protected fun disableTouch() {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    protected fun enableTouch() = activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

    private fun startObservingLiveData()  = observeLiveData(viewModel, binding)

}
