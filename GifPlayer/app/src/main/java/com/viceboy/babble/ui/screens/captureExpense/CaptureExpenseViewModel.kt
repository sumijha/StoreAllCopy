package com.viceboy.babble.ui.screens.captureExpense

import androidx.camera.core.FlashMode
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.viceboy.babble.ui.base.BaseViewModel
import javax.inject.Inject

class CaptureExpenseViewModel @Inject constructor() : BaseViewModel<Int>() {

    private var flashModeIndex = 0
    private val mapOfFlashMode = mapOf(0 to FlashMode.AUTO, 1 to FlashMode.ON, 2 to FlashMode.OFF)

    private val mutableFlashModeLiveData = MutableLiveData<FlashMode>(mapOfFlashMode[0])
    val flashModeLiveData: LiveData<FlashMode>
        get() = mutableFlashModeLiveData
    val flashModeIndexLiveData = Transformations.map(mutableFlashModeLiveData) {
        return@map if (it == FlashMode.AUTO) 0 else if (it == FlashMode.ON) 1 else 2
    }

    private val mutableBottomNavVisibilityLiveData = MutableLiveData<Boolean>()
    val bottomNavVisibilityLiveData: LiveData<Boolean>
        get() = mutableBottomNavVisibilityLiveData

    private val mutableEnablingShutterButtonLiveData = MutableLiveData<Boolean>(true)
    val enablingShutterButtonLiveData: LiveData<Boolean>
        get() = mutableEnablingShutterButtonLiveData


    fun onToggleFlashButtonClick() {
        if (flashModeIndex == 2) flashModeIndex = 0 else flashModeIndex += 1
        mutableFlashModeLiveData.value = mapOfFlashMode[flashModeIndex]
    }

    fun disableShutterButtonClick() {
        mutableEnablingShutterButtonLiveData.value = false
    }

    fun enableShutterButtonClick() {
        mutableEnablingShutterButtonLiveData.value = true
    }

    fun setVisibilityForBottomNav() {
        mutableBottomNavVisibilityLiveData.value = true
    }

    private fun resetBottomNavVisibilityLiveData() {
        mutableBottomNavVisibilityLiveData.value = false
    }

    override fun onCleared() {
        resetBottomNavVisibilityLiveData()
        super.onCleared()
    }
}