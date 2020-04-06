package com.viceboy.babble.di

import com.viceboy.babble.AuthActivity
import com.viceboy.babble.MainActivity
import com.viceboy.babble.auth.AuthProviderModule
import com.viceboy.babble.data_provider.mockRepository.RepositoryProviderModule
import com.viceboy.data_repo.converters.BeanConverterProviderModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBindingModule {

    @MainActivityScope
    @ContributesAndroidInjector(modules = [BeanConverterProviderModule::class, MainViewModelModule::class,MainFragmentBindingModule::class,RepositoryProviderModule::class])
    abstract fun contributeMainActivity(): MainActivity

    @AuthActivityScope
    @ContributesAndroidInjector(modules = [AuthFragmentBindingModule::class, AuthUtilsModule::class, AuthProviderModule::class, AuthViewModelModule::class])
    abstract fun contributeAuthActivity(): AuthActivity
}