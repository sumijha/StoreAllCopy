package com.viceboy.babble.auth

import com.viceboy.babble.di.MainActivityScope
import com.viceboy.babble.ui.base.AuthStateListener
import dagger.Binds
import dagger.Module

@Module
abstract class AuthListenerModule {

    @Binds
    @MainActivityScope
    abstract fun bindsAuthListenerModule(authStateListener: MockAuthStateListener):AuthStateListener
}