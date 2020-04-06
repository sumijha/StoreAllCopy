package com.viceboy.babble.ui.screens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.ui.base.LoginAuthProvider
import com.viceboy.babble.ui.base.BaseAuthViewModel
import com.viceboy.babble.ui.base.SingleLiveEvent
import com.viceboy.babble.ui.state.ButtonState
import com.viceboy.babble.ui.state.Resource
import com.viceboy.babble.ui.util.addToCompositeDisposable
import com.viceboy.babble.ui.util.isEmailPattern
import com.viceboy.babble.ui.util.isMobileNoPattern
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginAuthProvider: LoginAuthProvider) :
    BaseAuthViewModel<Int>() {

    private lateinit var userNameText: String
    private lateinit var passwordText: String

    /**
     * Exposing Mutable LiveData for two way databinding
     */
    val editEmailTextContent = MutableLiveData<String>()
    val editPasswordTextContent = MutableLiveData<String>()

    override val mutableListOfValidFields: MutableSet<String> = mutableSetOf()
    override val listOfFields: List<String> = listOf(KEY_USERNAME, KEY_PASSWORD)

    /**
     * Mediator LiveData to validate blank input fields
     */
    private val _mutableHasAllFieldsFilledLiveData = MediatorLiveData<Set<String>>()
    override val hasAllRequiredFieldsLiveData: LiveData<Boolean> =
        Transformations.map(_mutableHasAllFieldsFilledLiveData) {
            return@map it.containsAll(listOfFields)
        }

    override fun addSourcesToRequiredFieldsLiveData() {
        _mutableHasAllFieldsFilledLiveData.apply {
            addSourcesToMediatorLiveData(
                editEmailTextContent, this, mutableListOfValidFields, KEY_USERNAME
            ) {
                userNameText = it
                it.isEmailPattern() || it.isMobileNoPattern()
            }

            addSourcesToMediatorLiveData(
                editPasswordTextContent, this, mutableListOfValidFields, KEY_PASSWORD
            ) {
                passwordText = it
                it.length >= 5
            }
        }
    }

    /**
     * Clearing sources for MediatorLiveData which enables Button
     */
    override fun clearSourcesEnablingMediatorLiveData() {
        _mutableHasAllFieldsFilledLiveData.apply {
            removeSource(editEmailTextContent)
            removeSource(editPasswordTextContent)
        }
    }

    /**
     * Setting up Mediator LiveData to validate if User is logged in or not
     */
    private val mediatorLoginStateLiveData = MediatorLiveData<SingleLiveEvent<Resource<Any>>>()
    val loginStateLiveData: LiveData<SingleLiveEvent<Resource<Any>>>
        get() = mediatorLoginStateLiveData


    /**
     * Add Sources to MediatorLoginStateLiveData to authenticate User
     */
    private fun addSourceToLoginStateLiveData() {
        mediatorLoginStateLiveData.apply {
            addSource(onAnimationEndLiveData) {
                if (it) {
                    value = SingleLiveEvent(Resource.Loading())
                    loginAuthProvider.authenticateUser(userNameText, passwordText)
                        .addToCompositeDisposable(compositeDisposable, {
                            value = SingleLiveEvent(Resource.Success(null))
                            mediatorButtonStateManagerLiveData.value = ButtonState.INVISIBLE
                        }, {
                            value = SingleLiveEvent(Resource.Failure<String>(it.message))
                        })
                }
            }
        }
    }

    init {
        addSourceToEnableButtonChecklistLiveData()
        addSourcesToRequiredFieldsLiveData()
        addSourceToLoginStateLiveData()
        addSourceToMediatorButtonStateLiveData()

    }

    companion object {
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }
}