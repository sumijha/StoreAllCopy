package com.viceboy.babble.ui.screens.verifyPhone

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.navArgs
import com.viceboy.babble.R
import com.viceboy.babble.databinding.FragmentVerifyPhoneBinding
import com.viceboy.babble.di.Injectable
import com.viceboy.babble.ui.base.BaseAuthFragment
import com.viceboy.babble.ui.base.SignUpAuthProvider
import com.viceboy.babble.ui.state.OtpSendingState
import com.viceboy.babble.ui.state.OtpVerificationState
import com.viceboy.babble.ui.util.PreferenceUtil
import com.viceboy.babble.ui.util.addToCompositeDisposable
import javax.inject.Inject


class VerifyPhoneFragment : BaseAuthFragment<VerifyPhoneViewModel, FragmentVerifyPhoneBinding>(),
    Injectable {

    @Inject
    lateinit var preferenceManager: PreferenceUtil

    @Inject
    lateinit var signUpAuthProvider: SignUpAuthProvider

    @Inject
    lateinit var navController: NavController

    private var otpText: String? = null
    private val args by navArgs<VerifyPhoneFragmentArgs>()

    override fun layoutRes(): Int = R.layout.fragment_verify_phone

    override fun onCreateView() {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpBinding()
        setUpPhoneNumberListener()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun observeLiveData(
        viewModel: VerifyPhoneViewModel,
        binding: FragmentVerifyPhoneBinding
    ) {
        with(viewModel) {
            otpProviderStateLiveData.observe(viewLifecycleOwner, Observer { state ->
                if (state is OtpSendingState.Sent) otpText = state.data

            })

            otpVerificationStateLiveData.observe(viewLifecycleOwner, Observer { state ->
                if (state == OtpVerificationState.SUCCESS) {
                    val user = args.userDetails
                    val phoneText = binding.etVerifyPhone.text.toString()
                    if (phoneText != user.phone)
                        user.phone = phoneText
                    signUpAuthProvider.createNewUser(user, args.password)
                        .addToCompositeDisposable(compositeDisposable, {
                            onRegistrationSuccess()
                        }, {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        })
                }
            })

            triggerVerifyOtpLiveData.observe(viewLifecycleOwner, Observer {
                if (it) {
                    otpText?.let {
                        viewModel.verifyOtp(it)
                    }
                }
            })

            getOtpButtonStateManagerLiveData.observe(viewLifecycleOwner, Observer {
                binding.getOtpButtonState = it
            })

            verifyOtpButtonStateManagerLiveData.observe(viewLifecycleOwner, Observer {
                binding.verifyOtpButtonState = it
            })

        }
    }

    override val viewModelClass: Class<VerifyPhoneViewModel> = VerifyPhoneViewModel::class.java

    private fun setUpBinding() {
        binding.apply {
            verifyViewModel = viewModel
            lifecycleOwner = viewLifecycleOwner
            user = args.userDetails
        }
    }

    private fun onRegistrationSuccess() {
        preferenceManager.setUserIsLoggedIn(true)
        goToMainActivity()
    }

    private fun setUpPhoneNumberListener() = viewModel.initTextChanges(binding.etVerifyPhone)

    private fun goToMainActivity() {
        navController.navigate(VerifyPhoneFragmentDirections.actionVerifyPhoneFragmentToMainActivity())
        activity?.finish()
    }
}
