package com.viceboy.data_repo.repository

import com.viceboy.data_repo.model.dataModel.Groups
import io.reactivex.Completable
import io.reactivex.Flowable

interface GroupsRepository {
    fun loadGroup(groupId: String) : Flowable<Groups>
    fun loadGroupList(groupId: Array<String>): Flowable<List<Groups>>
    fun loadGroupMembers(groupId: String): Flowable<List<String>>
    fun loadGroupExpenses(groupId: String): Flowable<List<String>>
    fun loadUserExpenseMap(groupId: String): Flowable<MutableMap<String, Float>>
    fun saveGroup(groups: Groups): Completable
    fun remove(groups: Groups): Completable
}