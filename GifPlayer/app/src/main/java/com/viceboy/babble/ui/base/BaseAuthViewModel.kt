package com.viceboy.babble.ui.base

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import androidx.lifecycle.*
import com.viceboy.babble.ui.state.ButtonState
import io.reactivex.disposables.CompositeDisposable

abstract class BaseAuthViewModel<ND> : ViewModel() {

    protected val compositeDisposable = CompositeDisposable()
    private var animatorList: ArrayList<Animator>? = null

    /**
     * MediatorLiveData to observe its state
     */
    protected val mediatorButtonStateManagerLiveData = MediatorLiveData<ButtonState>()
    val buttonStateManagerLiveData: LiveData<ButtonState>
        get() = mediatorButtonStateManagerLiveData

    protected abstract val hasAllRequiredFieldsLiveData: LiveData<Boolean>
    protected abstract val mutableListOfValidFields: MutableSet<String>
    protected abstract val listOfFields: List<String>

    /**
     * Clearing sources for MediatorLiveData which enables Button
     */
    protected abstract fun clearSourcesEnablingMediatorLiveData()

    protected abstract fun addSourcesToRequiredFieldsLiveData()

    /**
     * Setting up LiveData for Navigation
     */
    private val _navigateToDestinationLiveData = MutableLiveData<SingleLiveEvent<ND>>()
    val navigateToDestinationLiveData: LiveData<SingleLiveEvent<ND>>
        get() = _navigateToDestinationLiveData

    fun setNavigationFlag(destination: ND) {
        _navigateToDestinationLiveData.value = SingleLiveEvent(destination)
    }


    /**
     * Setting up LiveData for SignUp Click Event
     */
    private val mutableIsClickedOnButtonLiveData = MutableLiveData<Boolean>(false)
    val onClickEventLiveData: LiveData<Boolean>
        get() = mutableIsClickedOnButtonLiveData

    fun onButtonClick() {
        mutableIsClickedOnButtonLiveData.value = true
    }

    fun resetButtonClickEvent() {
        mutableIsClickedOnButtonLiveData.value = false
    }


    /**
     * Setting up MediatorLiveData to Enable Button Functionality
     */
    private val checkListToEnableButtonFunc = listOf(KEY_BUTTON_EVENT, KEY_FIELD_VALIDATED)
    private val mutableCheckListToEnableButtonFunc = mutableSetOf<String>()
    private val _mediatorHasClickEventAndValidFieldLiveData = MediatorLiveData<Set<String>>()
    val hasClickEventAndValidFieldLiveData: LiveData<Boolean> =
        Transformations.map(_mediatorHasClickEventAndValidFieldLiveData) {
            return@map it.containsAll(checkListToEnableButtonFunc)
        }

    /**
     * Adding required field LiveData and signup click LiveData to trigger Create Account on Click
     */
    protected fun addSourceToEnableButtonChecklistLiveData() {
        _mediatorHasClickEventAndValidFieldLiveData.apply {
            addSource(hasAllRequiredFieldsLiveData) {
                if (it)
                    mutableCheckListToEnableButtonFunc.add(KEY_FIELD_VALIDATED)
                else
                    mutableCheckListToEnableButtonFunc.removeAll(checkListToEnableButtonFunc)
                value = mutableCheckListToEnableButtonFunc
            }

            addSource(mutableIsClickedOnButtonLiveData) {
                if (it && mutableListOfValidFields.containsAll(listOfFields))
                    mutableCheckListToEnableButtonFunc.add(KEY_BUTTON_EVENT)
                else
                    mutableCheckListToEnableButtonFunc.remove(KEY_BUTTON_EVENT)
                value = mutableCheckListToEnableButtonFunc
            }
        }
    }

    /**
     * Adding Sources to Mediator Button State
     */
    protected fun addSourceToMediatorButtonStateLiveData() {
        mediatorButtonStateManagerLiveData.apply {
            value = ButtonState.DISABLE
            addSource(_mediatorHasClickEventAndValidFieldLiveData) {
                if (it.containsAll(checkListToEnableButtonFunc))
                    value = ButtonState.CLICKED
            }

            addSource(hasAllRequiredFieldsLiveData) {
                value = if (it)
                    ButtonState.ACTIVE
                else
                    ButtonState.DISABLE
            }
        }
    }

    /**
     * Clear Mediator LiveData sources
     */
    private fun clearMediatorLiveDataSources() {
        _mediatorHasClickEventAndValidFieldLiveData.apply {
            removeSource(hasAllRequiredFieldsLiveData)
            removeSource(mutableIsClickedOnButtonLiveData)

            mutableCheckListToEnableButtonFunc.clear()
        }
    }

    /**
     * Reset Animation State
     */
    fun resetAnimationState() {
        animatorList?.let {
            for (va in it) {
                (va as ValueAnimator).reverse()
                va.end()
            }
        }
        mediatorButtonStateManagerLiveData.value = ButtonState.ACTIVE
    }

    /**
     * Adding sources to Mediator LiveData
     */
    protected fun addSourcesToMediatorLiveData(
        mutableLiveData: MutableLiveData<String>,
        liveData: MediatorLiveData<Set<String>>,
        mutableList: MutableSet<String>,
        keyVal: String,
        boolExpr: (String) -> Boolean
    ) {
        liveData.apply {
            addSource(mutableLiveData) {
                if (boolExpr(it))
                    mutableList.add(keyVal)
                else
                    mutableList.remove(keyVal)
                value = mutableList
            }
        }
    }

    private fun dispose() {
        if (!compositeDisposable.isDisposed)
            compositeDisposable.clear()
    }

    override fun onCleared() {
        super.onCleared()
        mutableListOfValidFields.clear()
        clearMediatorLiveDataSources()
        clearSourcesEnablingMediatorLiveData()
        dispose()
    }

    /**
     * Setting up Animation Listener for animation end
     */
    protected open val onAnimationEndLiveData = MutableLiveData<Boolean>(false)
    val onClickBtnAnimationListener = object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
        }

        override fun onAnimationEnd(animation: Animator?) {
            onAnimationEndLiveData.value = true
            resetButtonClickEvent()
            animatorList = (animation as AnimatorSet).childAnimations
            mediatorButtonStateManagerLiveData.value = ButtonState.INVISIBLE
        }

        override fun onAnimationCancel(animation: Animator?) {
        }

        override fun onAnimationStart(animation: Animator?) {
            mediatorButtonStateManagerLiveData.value = ButtonState.ANIMATING
        }

    }

    companion object {
        private const val KEY_FIELD_VALIDATED = "fields_checked"
        private const val KEY_BUTTON_EVENT = "button_clicked"
    }
}