package com.viceboy.babble.ui.screens.login

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentLoginBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseAuthFragment
import com.viceboy.babble.ui.base.EventObserver
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.PreferenceUtil
import javax.inject.Inject

class LoginFragment : BaseAuthFragment<LoginViewModel, FragmentLoginBinding>(), Injectable {

    @Inject
    lateinit var preferenceManager: PreferenceUtil

    @Inject
    lateinit var navController: NavController

    override fun onCreateView() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpBinding()
        super.onViewCreated(view, savedInstanceState)
    }


    override fun observeLiveData(viewModel: LoginViewModel, binding: FragmentLoginBinding) {
        with(viewModel) {
            onClickEventLiveData.observe(viewLifecycleOwner, Observer {
                binding.startFieldValidation = it
            })

            buttonStateManagerLiveData.observe(viewLifecycleOwner, Observer {
                binding.buttonState = it
            })

            loginStateLiveData.observe(viewLifecycleOwner, EventObserver { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.enableProgress = true
                        disableTouch()
                    }

                    is Resource.Success -> {
                        enableTouch()
                        binding.enableProgress = false
                        preferenceManager.setUserIsLoggedIn(true)
                        resetAnimationState()
                        Toast.makeText(
                            requireContext(),
                            "User is successfully logged in",
                            Toast.LENGTH_SHORT
                        ).show()
                        goToMainActivity()
                    }

                    is Resource.Failure -> {
                        enableTouch()
                        binding.enableProgress = false
                        resetAnimationState()
                        Toast.makeText(
                            requireContext(),
                            "${resource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            navigateToDestinationLiveData.observe(viewLifecycleOwner, EventObserver {
                navController.navigate(it)
            })
        }
    }

    override val viewModelClass: Class<LoginViewModel> = LoginViewModel::class.java

    override fun layoutRes(): Int = R.layout.fragment_login

    private fun setUpBinding() {
        binding.apply {
            authViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            signUpNavDirection = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            forgotPwdNavDirection = LoginFragmentDirections.actionLoginFragmentToForgotPwdFragment()
            loginButtonAnimationListener = viewModel.onClickBtnAnimationListener
            loginWithAnime = viewModel.hasClickEventAndValidFieldLiveData
            startAnimation = true
        }
    }

    private fun goToMainActivity() {
        navController.navigate(LoginFragmentDirections.actionLoginFragmentToMainActivity())
        activity?.finish()
    }

}
