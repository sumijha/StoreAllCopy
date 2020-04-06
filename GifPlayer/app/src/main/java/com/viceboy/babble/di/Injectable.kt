package com.viceboy.babble.di

import android.app.Application
import android.content.Context
import com.viceboy.babble.ui.util.PreferenceUtil
import dagger.Module
import dagger.Provides

@Module
class AppModule{

    @Provides
    fun providesContext(application: Application): Context = application.baseContext
}

interface Injectable