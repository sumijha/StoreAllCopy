package com.viceboy.babble.auth

import android.content.Context
import com.viceboy.babble.R
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.babble.ui.base.PhoneAuthProvider
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnMain
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

class MockPhoneAuthProvider @Inject constructor(private val context: Context) : PhoneAuthProvider {

    private val method = "get_users"
    private val endPoint = "auth/get_users"

    override fun requestVerificationOtp(phoneNumber: String): Observable<String> {
        return Observable.create<String> { emitter ->
            val users = MockResourceLoader.getResponseFrom(
                context,
                method,
                endPoint.split("/").toTypedArray()
            )
            users?.let { listOfUsers ->
                for (user in listOfUsers) {
                    if (user.phone.contains(phoneNumber)) {
                        emitter.onNext(otp)
                        return@create
                    }
                }
                emitter.onError(RuntimeException(context.getString(R.string.invalid_mobile_error)))
            }
        }.scheduleOnBackAndOutOnMain()
    }

    override fun verifyOtp(receivedOtp: String, enteredOtp: String): Completable {
        return Completable.defer {
            Completable.create { emitter ->
                if (receivedOtp == enteredOtp)
                    emitter.onComplete()
                else
                    emitter.onError(RuntimeException(context.getString(R.string.otp_verify_error)))
            }
        }
    }

    companion object {
        private const val otp = "123456"
    }
}

