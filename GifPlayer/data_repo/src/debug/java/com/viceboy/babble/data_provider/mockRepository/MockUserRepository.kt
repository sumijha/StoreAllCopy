package com.viceboy.babble.data_provider.mockRepository

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.data_repo.model.dataModel.User
import com.viceboy.data_repo.repository.UserRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class MockUserRepository @Inject constructor(val context: Context) : UserRepository {

    private val mutableCachedMapOfUser = HashMap<String, User>()
    private var mapOfUserId: LinkedTreeMap<String, Any>? = null

    override fun loadUser(userId: String): Flowable<User> {
        return Flowable.create({ emitter ->
            mapOfUserId?.let {
                MockResourceLoader.getModelData(
                    userId,
                    mutableCachedMapOfUser,
                    it
                ).subscribe({ user ->
                    emitter.onNext(user)
                }, { emitter.onError(it) }, { emitter.onComplete() })
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadUserList(userId: Array<String>): Flowable<List<User>> {
        Timber.e("New map is $mapOfUserId")
        return Flowable.create({ emitter ->
            mapOfUserId?.let {
                MockResourceLoader.getModelData<User>(
                    userId,
                    it
                ).subscribe({ listOfUser ->
                    if (listOfUser.isNotEmpty())
                        emitter.onNext(listOfUser)
                    else
                        emitter.onComplete()
                }, {
                    emitter.onError(it)
                }, {
                    emitter.onComplete()
                })
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadUserByEmail(email: String): Flowable<User> {
        return Flowable.create({ emitter ->
            mapOfUserId?.let { map ->
                val modelData = MockResourceLoader.queryDataWithParams<User>(
                    map,
                    "email",
                    email
                )

                if (modelData != null)
                    emitter.onNext(modelData)
                else
                    emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadGroups(userId: String): Flowable<List<String>> {
        return Flowable.create({ emitter ->
            mapOfUserId?.let {
                MockResourceLoader.getModelData(
                    userId,
                    mutableCachedMapOfUser,
                    it
                ).subscribe({ user ->
                    if (user.groups.isNullOrEmpty())
                        emitter.onComplete()
                    else
                        emitter.onNext(user.groups)
                }, {
                    emitter.onError(it)
                }, {
                    emitter.onComplete()
                })

            }
        }, BackpressureStrategy.LATEST)
    }

    override fun getCurrentUserId(): String = currentUserId


    private fun mapOfUsers(): LinkedTreeMap<String, Any>? {
        val mapOfUsers = MockResourceLoader.getDummyResponseFrom(
            context,
            method,
            endPoint.split("/").toTypedArray()
        )
        return mapOfUsers?.get(NODE_USERS) as LinkedTreeMap<String, Any>
    }


    private fun initMapOfUSerId() {
        mapOfUserId = mapOfUsers()
    }

    init {
        initMapOfUSerId()
    }

    companion object {
        private const val NODE_USERS = "users"

        private const val method = "users_data"
        private const val endPoint = "data/users_data"
        private const val currentUserId = "3qOzMCDPq6d6VTfzMv7HkKm8ifo2"
    }
}