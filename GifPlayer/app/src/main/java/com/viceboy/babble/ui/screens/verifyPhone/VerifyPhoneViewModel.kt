package com.viceboy.babble.ui.screens.verifyPhone

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import androidx.lifecycle.*
import com.google.android.material.textfield.TextInputEditText
import com.jakewharton.rxbinding3.widget.textChanges
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.BaseViewModel
import com.viceboy.babble.ui.base.PhoneAuthProvider
import com.viceboy.babble.ui.state.ButtonState
import com.viceboy.babble.ui.state.OtpSendingState
import com.viceboy.babble.ui.state.OtpVerificationState
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.isMobileNoPattern
import com.viceboy.babble.ui.util.observeOnMainThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VerifyPhoneViewModel @Inject constructor(
    private val phoneAuthProvider: PhoneAuthProvider,
    context: Context
) :
    BaseViewModel<Int>() {

    val mutablePinViewOtpTextLiveData = MutableLiveData<String>()

    private val timeOut = context.resources.getString(R.string.timeout)
    private val errorVerifyOtp =
        context.resources.getString(R.string.error_occured_while_verifying_otp)
    private val errorOtpSent =
        context.resources.getString(R.string.error_occured_while_sending_sms)
    private val infoOtpSent = context.resources.getString(R.string.otp_sent_on_mobile)
    private val infoOtpVerified = context.resources.getString(R.string.on_otp_verification_success)

    private val progressContext = viewModelScope.coroutineContext + Dispatchers.Main

    private lateinit var enteredOtpText: String
    private lateinit var phoneNumber: String


    /**
     * Setting up MediatorLiveData to show error Msg
     */
    private val mediatorShowErrorMsgLiveData = MediatorLiveData<Boolean>()
    val showErrorMsgLiveData: LiveData<Boolean>
        get() = mediatorShowErrorMsgLiveData

    /**
     * Mutable LiveData to create a placeholder for error messages
     */
    private val mediatorErrorTextPlaceholderLiveData = MediatorLiveData<String>()
    val errorTextPlaceholderLiveData: LiveData<String>
        get() = mediatorErrorTextPlaceholderLiveData

    /**
     * Mutable LiveData to trigger verify Otp Method
     */
    private val mutableCheckListToTriggerVerifyOtp = mutableSetOf<String>()
    private val checkListToTriggerVerifyOtp = listOf(KEY_BUTTON_CLICKED, KEY_OTP_ENTERED)
    private val mediatorTriggerVerifyOtpLiveData = MediatorLiveData<Set<String>>()
    val triggerVerifyOtpLiveData = Transformations.map(mediatorTriggerVerifyOtpLiveData) {
        return@map mutableCheckListToTriggerVerifyOtp.containsAll(checkListToTriggerVerifyOtp)
    }

    /**
     * Mutable LiveData to manager Otp Verification State
     */
    private val mutableOtpVerificationState = MutableLiveData<OtpVerificationState>()
    val otpVerificationStateLiveData: LiveData<OtpVerificationState>
        get() = mutableOtpVerificationState

    /**
     * Mutable LiveDATA to send text to TextView
     */
    private var otpVerificationSubHeaderText: String? = null
    private val mediatorOtpVerificationSubHeaderTextLiveData = MediatorLiveData<String>()
    val otpVerificationSubHeaderTextLiveData: LiveData<String>
        get() = mediatorOtpVerificationSubHeaderTextLiveData

    /**
     * Mutable LiveData to show resend SMS text link to request OTP again
     */
    private val mutableResendOtpTextLiveData = MutableLiveData<Boolean>()
    val resendOtpTextLiveData: LiveData<Boolean>
        get() = mutableResendOtpTextLiveData

    /**
     * Setting up Mutable LiveData to manage Phone Auth Provider LOADING, SUCCESS or FAILURE state
     */
    private val mutableOtpProviderStateLiveData = MutableLiveData<OtpSendingState<Any>>()
    val otpProviderStateLiveData: LiveData<OtpSendingState<Any>>
        get() = mutableOtpProviderStateLiveData

    /**
     * GetOtp Button MediatorLiveData to manage its state
     */
    private val mediatorGetOtpButtonStateManagerLiveData = MediatorLiveData<ButtonState>()
    val getOtpButtonStateManagerLiveData: LiveData<ButtonState>
        get() = mediatorGetOtpButtonStateManagerLiveData

    /**
     * Verify & SignUP Button MediatorLiveData to manage its state
     */
    private val mediatorVerifyOtpButtonStateManagerLiveData = MediatorLiveData<ButtonState>()
    val verifyOtpButtonStateManagerLiveData: LiveData<ButtonState>
        get() = mediatorVerifyOtpButtonStateManagerLiveData

    /**
     * Setting up Mutable LiveData to show Otp Progress Timer
     */
    private val mediatorEnableOtpProgressTimer = MediatorLiveData<Boolean>()
    val enableOtpProgressTimerLiveData: LiveData<Boolean>
        get() = mediatorEnableOtpProgressTimer


    /**
     * Add Sources to GetOtpButtonStateManagerLiveData
     */
    private fun addSourcesToGenerateOtpButtonMediatorLiveData() {
        mediatorGetOtpButtonStateManagerLiveData.apply {
            addSource(mediatorGetOtpButtonStateManagerLiveData) {
                if (it == ButtonState.INVISIBLE) {
                    requestOtp()
                }
            }

            addSource(mutableOtpProviderStateLiveData) {
                if (it is OtpSendingState.Failed) {
                    resetAnimationState(getOtpAnimatorList)
                    value = ButtonState.ACTIVE
                }
            }

            addSource(onGenerateOtpAnimationEndLiveData) {
                value = if (it)
                    ButtonState.INVISIBLE
                else
                    ButtonState.ANIMATING
            }
        }
    }

    /**
     * Add Sources to ShowOtpProgressTimer MediatorLiveData to enable and disable progress
     */
    private fun addSourcesToShowOtpTimerMediatorLiveData() {
        mediatorEnableOtpProgressTimer.apply {
            addSource(mutableOtpProviderStateLiveData) {
                value = it is OtpSendingState.Loading
            }

            addSource(progressLiveData) {
                if (it == timeOut.toInt())
                    value = false
            }
        }
    }

    /**
     * Add Sources to MediatorShowErrorMsgLiveData to enable or disable visibility
     */
    private fun addSourcesToShowErrorTextMediatorLiveData() {
        mediatorShowErrorMsgLiveData.apply {
            addSource(mutableOtpVerificationState) {
                value = it == OtpVerificationState.FAILED
            }

            addSource(mutableOtpProviderStateLiveData) {
                value = it is OtpSendingState.Failed
            }
        }
    }

    /**
     * Add Sources to verify Otp MediatorLiveData
     */
    private fun addSourcesToVerifyOtpButtonMediatorLiveData() {
        mediatorVerifyOtpButtonStateManagerLiveData.apply {
            addSource(mediatorGetOtpButtonStateManagerLiveData) {
                value = if (it == ButtonState.INVISIBLE)
                    ButtonState.DISABLE
                else
                    ButtonState.INVISIBLE

            }

            addSource(mutablePinViewOtpTextLiveData) {
                value = if (it.length == 6)
                    ButtonState.ACTIVE
                else
                    ButtonState.DISABLE
            }

            addSource(onVerifyOtpAnimationEndLiveData) {
                value = if (it) {
                    ButtonState.INVISIBLE
                } else
                    ButtonState.ANIMATING

            }

            addSource(mutableOtpVerificationState) {
                if (it == OtpVerificationState.FAILED) {
                    resetAnimationState(verifyOtpAnimatorList)
                    value = ButtonState.ACTIVE
                }
            }
        }
    }

    /**
     * Add Sources to mediatorErrorMsgPlaceholderLiveData to show error message
     */
    private fun addSourcesToErrorMsgPlaceholderMediatorLiveData() {
        mediatorErrorTextPlaceholderLiveData.apply {
            addSource(mutableOtpVerificationState) {
                if (it == OtpVerificationState.FAILED)
                    value = errorVerifyOtp
            }

            addSource(mutableOtpProviderStateLiveData) {
                if (it is OtpSendingState.Failed)
                    value = errorOtpSent
            }
        }
    }

    /**
     * Add Source to mediatorTriggerVerifyOtpLiveData to trigger VerifyOtp method
     */
    private fun addSourceToTriggerVerifyOtpMediatorLiveData() {
        mediatorTriggerVerifyOtpLiveData.apply {
            addSource(mutablePinViewOtpTextLiveData) {
                enteredOtpText = it
                if (it.length == 6)
                    mutableCheckListToTriggerVerifyOtp.add(KEY_OTP_ENTERED)
                else
                    mutableCheckListToTriggerVerifyOtp.remove(KEY_OTP_ENTERED)
                value = mutableCheckListToTriggerVerifyOtp

            }

            addSource(mediatorVerifyOtpButtonStateManagerLiveData) {
                if (it == ButtonState.INVISIBLE)
                    mutableCheckListToTriggerVerifyOtp.add(KEY_BUTTON_CLICKED)
                else
                    mutableCheckListToTriggerVerifyOtp.remove(KEY_BUTTON_CLICKED)
                value = mutableCheckListToTriggerVerifyOtp
            }
        }
    }

    /**
     * Adding Sources to mediatorOtpVerificationSubHeaderTextLiveData to display appropriate text on SubHeader info
     */
    private fun addSourcesToOtpVerificationSubHeaderTextMediatorLiveData() {
        mediatorOtpVerificationSubHeaderTextLiveData.apply {
            addSource(mutableOtpVerificationState) {
                if (it == OtpVerificationState.SUCCESS)
                    value = infoOtpVerified
            }

            addSource(mutableOtpProviderStateLiveData) {
                if (it !is OtpSendingState.Failed)
                    value = "$infoOtpSent $otpVerificationSubHeaderText"
            }
        }
    }

    /**
     * Setting up generateOtp Click Animation Listener
     */
    private var getOtpAnimatorList: ArrayList<Animator>? = null
    private val onGenerateOtpAnimationEndLiveData = MutableLiveData<Boolean>(false)
    val getOtpButtonAnimatorListener = getAnimatorListenerObject(
        { animator ->
            getOtpAnimatorList = (animator as AnimatorSet).childAnimations
            onGenerateOtpAnimationEndLiveData.value = true
        }
        ,
        { onGenerateOtpAnimationEndLiveData.value = false }
    )

    /**
     * Setting up verifyOtp Click Animation Listener
     */
    private var verifyOtpAnimatorList: ArrayList<Animator>? = null
    private val onVerifyOtpAnimationEndLiveData = MutableLiveData<Boolean>(false)
    val verifyOtpButtonAnimatorListener = getAnimatorListenerObject(
        {
            verifyOtpAnimatorList = (it as AnimatorSet).childAnimations
            onVerifyOtpAnimationEndLiveData.value = true
        },
        { onVerifyOtpAnimationEndLiveData.value = false }
    )

    /**
     * Setting up LiveDATA to show timer count and set progress on OTP Progress Timer
     */
    val progressLiveData = Transformations.switchMap(mediatorEnableOtpProgressTimer) {
        if (it) {
            mutableResendOtpTextLiveData.value = false
            liveData(progressContext) {
                for (i in 1..60) {
                    delay(1000)
                    emit(i)
                }
                mediatorEnableOtpProgressTimer.value = false
                mutableResendOtpTextLiveData.value = true
            }
        } else {
            liveData { }
        }
    }

    /**
     * Setting up RxObservable to check phone number text changes
     */
    fun initTextChanges(textView: TextInputEditText) {
        compositeDisposable.add(
            textView.textChanges()
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOnMainThread()
                .subscribe(
                    {
                        phoneNumber = it.toString()
                        if (phoneNumber.isMobileNoPattern()) {
                            otpVerificationSubHeaderText = it.toString()
                            mediatorGetOtpButtonStateManagerLiveData.value = ButtonState.ACTIVE
                        } else
                            mediatorGetOtpButtonStateManagerLiveData.value = ButtonState.DISABLE
                    }, {
                        Timber.e(it.message)
                        phoneNumber = ""
                        otpVerificationSubHeaderText = it.toString()
                        mediatorGetOtpButtonStateManagerLiveData.value = ButtonState.DISABLE
                    }
                )
        )
    }

    /**
     * Method is called when GetOtp Button is clicked
     */
    fun onGetOtpButtonClicked() {
        onButtonClick(mediatorGetOtpButtonStateManagerLiveData)
    }

    /**
     * Method is called when GetOtp Button is clicked
     */
    fun onVerifyOtpButtonClicked() {
        onButtonClick(mediatorVerifyOtpButtonStateManagerLiveData)
    }

    /**
     * Request Verification Otp
     */
    fun requestOtp() {
        mutableOtpProviderStateLiveData.value = OtpSendingState.Loading()
        compositeDisposable.add(
            phoneAuthProvider.requestVerificationOtp(phoneNumber).subscribe(
                { data ->
                    mutableOtpProviderStateLiveData.value =
                        OtpSendingState.Sent(data)
                },
                { throwable ->
                    mutableOtpProviderStateLiveData.value =
                        OtpSendingState.Failed<Throwable>(throwable.message)
                },
                {
                    mutableOtpProviderStateLiveData.value =
                        OtpSendingState.Verified(null)
                })
        )
    }

    /**
     * Verify Otp and update Otp Verification LiveData Manager
     */
    fun verifyOtp(expectedOtp: String) {
        phoneAuthProvider.verifyOtp(expectedOtp, enteredOtpText)
            .addToCompositeDisposable(compositeDisposable, {
                mutableOtpVerificationState.value = OtpVerificationState.SUCCESS
            }, {
                mutableOtpVerificationState.value = OtpVerificationState.FAILED
            })
    }

    init {
        addSourcesToGenerateOtpButtonMediatorLiveData()
        addSourcesToShowOtpTimerMediatorLiveData()
        addSourcesToVerifyOtpButtonMediatorLiveData()
        addSourcesToErrorMsgPlaceholderMediatorLiveData()
        addSourcesToShowErrorTextMediatorLiveData()
        addSourceToTriggerVerifyOtpMediatorLiveData()
        addSourcesToOtpVerificationSubHeaderTextMediatorLiveData()
    }

    companion object {
        private const val KEY_OTP_ENTERED = "otp_entered"
        private const val KEY_BUTTON_CLICKED = "button_clicked"
    }

    override fun onCleared() {
        dispose()
        super.onCleared()
    }
}
