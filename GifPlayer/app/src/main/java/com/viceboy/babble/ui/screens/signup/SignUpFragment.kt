package com.viceboy.babble.ui.screens.signup

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentSignupBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseAuthFragment
import com.viceboy.babble.ui.base.EventObserver
import com.viceboy.babble.ui.state.ButtonState
import com.viceboy.babble.ui.util.toEditable
import javax.inject.Inject

//TODO:Add Country code check to enable SignUp Button
class SignUpFragment : BaseAuthFragment<SignUpViewModel, FragmentSignupBinding>(), Injectable {

    @Inject
    lateinit var navController: NavController

    override fun onCreateView() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAutoCompleteItemClickListener(binding)
        setUpBinding()
    }

    override fun layoutRes(): Int = R.layout.fragment_signup

    override fun observeLiveData(viewModel: SignUpViewModel, binding: FragmentSignupBinding) {
        with(viewModel) {
            binding.also { binding ->

                onClickEventLiveData.observe(viewLifecycleOwner, Observer {
                    binding.startFieldValidation = it
                })

                buttonStateManagerLiveData.observe(viewLifecycleOwner, Observer {
                    binding.buttonState = it
                    if (it == ButtonState.INVISIBLE) {
                        navController.navigate(
                            SignUpFragmentDirections.actionRegisterFragmentToVerifyPhoneFragment(
                                getUserModel(), binding.editSignupPassword.text.toString()
                            )
                        )
                        resetAnimationState()
                    }
                })

                invalidCountryCodeLiveData.observe(viewLifecycleOwner, Observer {
                    binding.countryCodeError = it
                })
            }
            navigateToDestinationLiveData.observe(viewLifecycleOwner, EventObserver {
                resetButtonClickEvent()
                navController.popBackStack()
            })
        }
    }

    override val viewModelClass: Class<SignUpViewModel>
        get() = SignUpViewModel::class.java

    /**
     * Initialize SignUp Fragment Binding values
     */
    private fun setUpBinding() {
        val arrayCodes: ArrayList<String> =
            resources.getStringArray(R.array.country_arrays).toList() as ArrayList<String>
        binding.apply {
            signUpViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            createAccountWithAnime = viewModel.hasClickEventAndValidFieldLiveData
            signUpAnimationListener = viewModel.onClickBtnAnimationListener
            countryCodes = arrayCodes
        }
    }

    /**
     * Initialize AutoCompleteText View Item Click Listener
     */
    private fun initAutoCompleteItemClickListener(binding: FragmentSignupBinding) {
        binding.autoTextViewCcp.apply {
            setOnItemClickListener { _, itemView, _, _ ->
                val itemText = (itemView as TextView).text.toString()
                val text = itemText.substring(itemText.indexOf("+"))
                binding.autoTextViewCcp.text = text.toEditable()
            }
        }
    }
}

