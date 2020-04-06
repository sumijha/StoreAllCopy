package com.viceboy.data_repo.repository

import com.viceboy.data_repo.model.dataModel.User
import io.reactivex.Flowable

interface UserRepository {
    fun getCurrentUserId(): String
    fun loadUser(userId: String): Flowable<User>
    fun loadUserByEmail(email:String) : Flowable<User>
    fun loadUserList(userId: Array<String>):Flowable<List<User>>
    fun loadGroups(userId: String): Flowable<List<String>>
}