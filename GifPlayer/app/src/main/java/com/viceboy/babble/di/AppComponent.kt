package com.viceboy.babble.di

import android.app.Application
import com.viceboy.babble.auth.AuthListenerModule
import com.viceboy.babble.ui.base.BaseApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Component(modules = [AndroidInjectionModule::class, AppModule::class, ActivityBindingModule::class, AuthListenerModule::class])
@Singleton
interface AppComponent : AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}