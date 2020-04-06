package com.viceboy.babble.di

import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.viceboy.babble.AuthActivity
import com.viceboy.babble.R
import com.viceboy.babble.ui.base.DisposableManager
import com.viceboy.babble.ui.screens.login.LoginFragment
import com.viceboy.babble.ui.screens.signup.SignUpFragment
import com.viceboy.babble.ui.screens.verifyPhone.VerifyPhoneFragment
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import javax.inject.Scope

@Module
abstract class AuthFragmentBindingModule {

    @ContributesAndroidInjector
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun contributeRegisterFragment(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun contributeVerifyPhoneFragment():VerifyPhoneFragment
}

@Module
class AuthUtilsModule {

    @Provides
    @AuthActivityScope
    fun providesNavController(activity: AuthActivity): NavController = Navigation.findNavController(
        activity,
        R.id.navAuthHost
    )
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthActivityScope

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainActivityScope