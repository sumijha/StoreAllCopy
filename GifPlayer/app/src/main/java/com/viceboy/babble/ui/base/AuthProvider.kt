package com.viceboy.babble.ui.base

import com.viceboy.data_repo.model.dataModel.User
import io.reactivex.Completable
import io.reactivex.Observable

interface LoginAuthProvider {
    fun authenticateUser(username:String,password:String):Completable
}

interface PhoneAuthProvider {
    fun requestVerificationOtp(phoneNumber:String) : Observable<String>
    fun verifyOtp(receivedOtp:String,enteredOtp:String):Completable
}

interface SignUpAuthProvider {
    fun createNewUser(user: User, password: String):Completable
}

interface AuthStateListener {
    fun hasValidSession():Boolean
}