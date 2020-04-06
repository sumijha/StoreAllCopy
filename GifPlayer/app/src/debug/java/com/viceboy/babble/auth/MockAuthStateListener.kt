package com.viceboy.babble.auth

import com.viceboy.babble.ui.base.AuthStateListener
import com.viceboy.babble.ui.util.PreferenceUtil
import javax.inject.Inject

class MockAuthStateListener @Inject constructor(private val preferenceUtil: PreferenceUtil) : AuthStateListener {

    override fun hasValidSession():Boolean = preferenceUtil.isUserLoggedIn()
}