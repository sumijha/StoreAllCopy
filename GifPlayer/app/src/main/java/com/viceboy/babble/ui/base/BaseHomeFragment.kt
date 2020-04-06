package com.viceboy.babble.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.viceboy.babble.R
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

abstract class BaseHomeFragment<VM : ViewModel, VB : ViewDataBinding> : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected lateinit var bottomNavigationView: BottomNavigationView

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
    protected abstract val hasBottomNavigationView: Boolean

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, layoutRes(), container, false)
        startObservingLiveData()
        initBottomNavigation()
        handleBottomNavVisibility()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onCreateView()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun handleBottomNavVisibility() {
        if (hasBottomNavigationView) {
            if (bottomNavigationView.visibility == View.GONE || bottomNavigationView.visibility == View.INVISIBLE)
                bottomNavigationView.visibility = View.VISIBLE
        } else {
            bottomNavigationView.visibility = View.GONE
        }
    }

    private fun initBottomNavigation() {
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)
    }

    private fun enableBottomBar(enable: Boolean) {
        for (i in 0 until bottomNavigationView.menu.size()) {
            bottomNavigationView.menu.getItem(i).isEnabled = enable
        }
    }

    private fun startObservingLiveData() = observeLiveData(viewModel, binding)

}
