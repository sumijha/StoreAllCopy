package com.viceboy.babble.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.viceboy.babble.ui.base.ViewModelFactory
import com.viceboy.babble.ui.screens.login.LoginViewModel
import com.viceboy.babble.ui.screens.signup.SignUpViewModel
import com.viceboy.babble.ui.screens.verifyPhone.VerifyPhoneViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import kotlin.reflect.KClass

@Module
abstract class AuthViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindsLoginViewModelModule(viewModel: LoginViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SignUpViewModel::class)
    abstract fun bindsSignupViewModelModule(viewModel: SignUpViewModel):ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerifyPhoneViewModel::class)
    abstract fun bindsVerifyPhoneViewModelModule(viewModel: VerifyPhoneViewModel):ViewModel

    @Binds
    abstract fun bindsViewModelFactory(viewModelFactory:ViewModelFactory):ViewModelProvider.Factory
}

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)