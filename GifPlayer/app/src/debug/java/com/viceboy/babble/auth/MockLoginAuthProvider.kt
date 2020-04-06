package com.viceboy.babble.auth

import android.content.Context
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.babble.ui.base.LoginAuthProvider
import com.viceboy.babble.ui.util.scheduleOnBackAndOutOnMain
import com.viceboy.data_repo.model.dataModel.User
import io.reactivex.Completable
import javax.inject.Inject

class MockLoginAuthProvider @Inject constructor(
    private val context: Context
) : LoginAuthProvider {

    private val DEBUG_PASSWORD = "123456"

    private val method = "get_users"
    private val endPoint = "auth/get_users"

    private lateinit var authenticatedUser: User

    override fun authenticateUser(
        username: String,
        password: String
    ): Completable {
        return Completable.defer {
            Completable.create { emitter ->
                val users = MockResourceLoader.getResponseFrom(
                    context,
                    method,
                    endPoint.split("/").toTypedArray()
                )
                users?.let { listOfUsers ->
                    for (user in listOfUsers) {
                        if ((user.email == username || user.phone == username) && password == DEBUG_PASSWORD) {
                            authenticatedUser = user
                            emitter.onComplete()
                            return@create
                        }
                    }
                    emitter.onError(RuntimeException("User has not registered yet"))
                }
            }.scheduleOnBackAndOutOnMain()
        }
    }
}

