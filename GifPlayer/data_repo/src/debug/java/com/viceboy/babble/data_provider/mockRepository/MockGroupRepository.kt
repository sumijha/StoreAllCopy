package com.viceboy.babble.data_provider.mockRepository

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.data_repo.model.dataModel.Groups
import com.viceboy.data_repo.repository.GroupsRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class MockGroupRepository @Inject constructor(val context: Context) : GroupsRepository {

    private val mutableCachedMapOfGroups = HashMap<String, Groups>()
    private var mapOfGroupId: LinkedTreeMap<String, Any>? = null

    override fun loadGroup(groupId: String): Flowable<Groups> {
        return Flowable.create({ emitter ->
            mapOfGroupId?.let {
                MockResourceLoader.getModelData(
                    groupId,
                    mutableCachedMapOfGroups,
                    it
                ).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        emitter.onNext(it)
                    }, {
                        emitter.onError(it)
                    }, {
                        emitter.onComplete()
                    })
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadGroupList(groupId: Array<String>): Flowable<List<Groups>> {
        return Flowable.create({ emitter ->
            mapOfGroupId?.let {
                MockResourceLoader.getModelData<Groups>(
                    groupId,
                    it
                ).subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({ listOfGroups ->
                        if (listOfGroups.isNotEmpty())
                            emitter.onNext(listOfGroups)
                        else
                            emitter.onComplete()
                    }, {
                        emitter.onError(it)
                    })

            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadGroupMembers(groupId: String): Flowable<List<String>> {
        return Flowable.create({ emitter ->
            mapOfGroupId?.let {
                MockResourceLoader.getModelData(
                    groupId,
                    mutableCachedMapOfGroups,
                    it
                ).subscribe({ group ->
                    val memberList = group.groupMembers.keys.toMutableList()
                    group.groupAdmin.keys.forEach { memberList.add(it) }
                    if (memberList.isNotEmpty())
                        emitter.onNext(memberList)
                    else
                        emitter.onComplete()
                }, {
                    emitter.onError(it)
                })

            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadGroupExpenses(groupId: String): Flowable<List<String>> {
        return Flowable.create({ emitter ->
            mapOfGroupId?.let {
                MockResourceLoader.getModelData(
                    groupId,
                    mutableCachedMapOfGroups,
                    it
                ).subscribe { group ->
                    val memberExpenses = ArrayList<String>()
                    group.groupMembers.entries.forEach { entrySet ->
                        val mapOfExpenses = entrySet.value as LinkedTreeMap<String, Any>
                        val expenses = mapOfExpenses["expenses"] as ArrayList<String>
                        memberExpenses.addAll(expenses)
                    }
                    group.groupAdmin.entries.forEach { entrySet ->
                        val mapOfExpenses = entrySet.value as LinkedTreeMap<String, Any>
                        val expenses = mapOfExpenses["expenses"] as ArrayList<String>
                        memberExpenses.addAll(expenses)
                    }
                    if (memberExpenses.isNotEmpty()) emitter.onNext(memberExpenses)
                    else emitter.onComplete()
                }
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadUserExpenseMap(groupId: String): Flowable<MutableMap<String, Float>> {
        return Flowable.create({ emitter ->
            mapOfGroupId?.let {
                MockResourceLoader.getModelData(groupId, mutableCachedMapOfGroups, it)
                    .subscribe { group ->
                        try {
                            val mapOfExpense = mutableMapOf<String, Float>()
                            group.groupMembers.entries.forEach { entrySet ->
                                val mapOfExpenses = entrySet.value as LinkedTreeMap<String, Any>
                                val expenseAmount = mapOfExpenses["amount"].toString().toFloat()
                                mapOfExpense[entrySet.key] = expenseAmount
                            }
                            group.groupAdmin.entries.forEach { entrySet ->
                                val mapOfExpenses = entrySet.value as LinkedTreeMap<String, Any>
                                val expenseAmount = mapOfExpenses["amount"].toString().toFloat()
                                mapOfExpense[entrySet.key] = expenseAmount
                            }

                            if (mapOfExpense.isNotEmpty()) emitter.onNext(mapOfExpense)
                            else emitter.onComplete()
                        } catch (exception: Exception) {
                            emitter.onError(exception)
                        }
                    }
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun saveGroup(groups: Groups): Completable {
        return Completable.create {}
    }

    override fun remove(groups: Groups): Completable {
        return Completable.create {}
    }

    private fun mapOfGroups(): LinkedTreeMap<String, Any> {
        val mapOfGroups = MockResourceLoader.getDummyResponseFrom(
            context,
            method,
            endPoint.split("/").toTypedArray()
        )
        return mapOfGroups?.get(NODE_GROUPS) as LinkedTreeMap<String, Any>
    }

    init {
        mapOfGroupId = mapOfGroups()
    }

    companion object {
        private const val NODE_GROUPS = "groups"

        private const val method = "users_groups"
        private const val endPoint = "data/users_groups"
    }
}