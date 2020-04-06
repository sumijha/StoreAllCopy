package com.viceboy.babble.auth

import com.viceboy.babble.ui.base.LoginAuthProvider
import com.viceboy.babble.ui.base.PhoneAuthProvider
import com.viceboy.babble.ui.base.SignUpAuthProvider
import dagger.Binds
import dagger.Module

@Module
abstract class  AuthProviderModule {

    @Binds
    abstract fun bindsAuthProviderModule(authProvider: MockLoginAuthProvider): LoginAuthProvider

    @Binds
    abstract fun bindsPhoneAuthProviderModule(phoneAuthProvider: MockPhoneAuthProvider):PhoneAuthProvider

    @Binds
    abstract fun bindsSignUpAuthProvider(signUpProvider: MockSignUpProvider) : SignUpAuthProvider
}