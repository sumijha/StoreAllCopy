package com.viceboy.babble.ui.util

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceUtil @Inject constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(KEY_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun isUserLoggedIn() = prefs.getBoolean(PREF_USER_LOGGED_IN, false)

    fun getHeight() = prefs.getInt(PREF_IMAGE_HEIGHT, 0)

    fun setProfileImageHeight(height: Int) {
        prefs.edit().putInt(PREF_IMAGE_HEIGHT, height).apply()
    }

    fun setUserIsLoggedIn(flag: Boolean) =
        prefs.edit().putBoolean(PREF_USER_LOGGED_IN, flag).apply()


    companion object {
        private const val KEY_SHARED_PREFERENCES_NAME = "com.viceboy.babble.preferences"
        private const val PREF_USER_LOGGED_IN = "user_logged_in"
        private const val PREF_IMAGE_HEIGHT = "image"
    }
}