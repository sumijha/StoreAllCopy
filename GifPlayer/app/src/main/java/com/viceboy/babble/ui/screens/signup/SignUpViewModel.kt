package com.viceboy.babble.ui.screens.signup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.BaseAuthViewModel
import com.viceboy.babble.ui.util.isEmailPattern
import com.viceboy.babble.ui.util.isMobileNoPattern
import com.viceboy.data_repo.model.dataModel.User
import java.util.*
import javax.inject.Inject

class SignUpViewModel @Inject constructor(context: Context) : BaseAuthViewModel<Boolean>() {

    private val listOfCountryCodes =
        context.resources.getStringArray(R.array.country_arrays).toList()

    private lateinit var email: String
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var phone: String
    private lateinit var countryCode: String
    private lateinit var country: String

    private var countryCodeErrorFlag: Boolean = true

    /**
     * Exposing Mutable LiveData to setup two way data binding for Input fields
     */
    val editSignUpInputName = MutableLiveData<String>()
    val editSignUpInputEmail = MutableLiveData<String>()
    val editSignUpInputPhone = MutableLiveData<String>()
    val editSignUpPassword = MutableLiveData<String>()
    val editSignUpCountryCode = MutableLiveData<String>("+91")

    override val listOfFields: List<String> =
        listOf(KEY_EMAIL, KEY_NAME, KEY_PASSWORD, KEY_PASSWORD, KEY_PHONE, KEY_COUNTRY_CODE)
    override val mutableListOfValidFields: MutableSet<String> = mutableSetOf()

    /**
     * Setting up LiveData to show error Flag for Invalid Country ocde
     */
    private val mutableInvalidCountryCodeLiveData = MutableLiveData<Boolean>(false)
    val invalidCountryCodeLiveData: LiveData<Boolean>
        get() = mutableInvalidCountryCodeLiveData

    /**
     * Mediator LiveData to validate blank input fields
     */
    private val _hasAllFieldsFilled = MediatorLiveData<Set<String>>()
    override val hasAllRequiredFieldsLiveData: LiveData<Boolean> =
        Transformations.map(_hasAllFieldsFilled) {
            return@map it.containsAll(listOfFields)
        }

    override fun addSourcesToRequiredFieldsLiveData() {
        _hasAllFieldsFilled.apply {
            addSourcesToMediatorLiveData(
                editSignUpInputEmail, this, mutableListOfValidFields,
                KEY_EMAIL
            ) {
                email = it
                it.isEmailPattern()
            }

            addSourcesToMediatorLiveData(
                editSignUpInputName, this, mutableListOfValidFields,
                KEY_NAME
            ) {
                username = it
                it.length > 2
            }

            addSourcesToMediatorLiveData(
                editSignUpInputPhone, this, mutableListOfValidFields,
                KEY_PHONE
            ) {
                phone = it
                if (countryCodeErrorFlag) mutableInvalidCountryCodeLiveData.value = true
                it.isMobileNoPattern()
            }

            addSourcesToMediatorLiveData(
                editSignUpPassword, this, mutableListOfValidFields,
                KEY_PASSWORD
            ) {
                password = it
                it.length >= 5
            }

            addSourcesToMediatorLiveData(
                editSignUpCountryCode, this, mutableListOfValidFields,
                KEY_COUNTRY_CODE
            ) { input ->
                countryCode = input
                var checkFlag = false
                listOfCountryCodes.forEach {
                    if (it.substring(it.indexOf("+")) == input) {
                        checkFlag = true
                        country = getCountryFromCountryCode(it)
                        return@forEach
                    }
                }
                countryCodeErrorFlag = !checkFlag
                mutableInvalidCountryCodeLiveData.value = !checkFlag
                return@addSourcesToMediatorLiveData checkFlag
            }
        }
    }

    /**
     * Clearing sources for MediatorLiveData which enables Button
     */
    override fun clearSourcesEnablingMediatorLiveData() {
        _hasAllFieldsFilled.apply {
            removeSource(editSignUpInputEmail)
            removeSource(editSignUpInputName)
            removeSource(editSignUpInputPhone)
            removeSource(editSignUpPassword)
        }
    }

    fun getUserModel(): User {
        val id = UUID.randomUUID().toString()
        return User(
            id,
            email,
            countryCode,
            country,
            username,
            phone,
            null,
            null
        )
    }

    private fun getCountryFromCountryCode(input: String) =
        input.split("+")[0].trim().substring(input.indexOf("(")+1,input.lastIndexOf(")"))


    init {
        addSourcesToRequiredFieldsLiveData()
        addSourceToEnableButtonChecklistLiveData()
        addSourceToMediatorButtonStateLiveData()
    }

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_NAME = "name"
        private const val KEY_PASSWORD = "password"
        private const val KEY_COUNTRY_CODE = "country_code"
    }
}