package com.viceboy.babble.auth

import android.os.SystemClock.sleep
import com.viceboy.babble.ui.base.SignUpAuthProvider

import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnMain
import com.viceboy.data_repo.model.dataModel.User
import io.reactivex.Completable
import javax.inject.Inject

class MockSignUpProvider @Inject constructor(): SignUpAuthProvider {

    private val DEBUG_PASSWORD = "123456"

    override fun createNewUser(user: User, password:String) : Completable {
        return Completable.defer {
            Completable.create {emitter ->
                if (registerUser())
                    emitter.onComplete()
                else
                    emitter.onError(RuntimeException("Failed to create new user, kindly check if you are connected to internet"))
            }
        }.scheduleOnBackAndOutOnMain()
    }

    private fun registerUser():Boolean {
        sleep(1000)
        return true
    }
}