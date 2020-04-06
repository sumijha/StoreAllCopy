package com.viceboy.babble.data_provider.mockRepository

import android.content.Context
import com.google.gson.internal.LinkedTreeMap
import com.viceboy.babble.data_provider.mockDataLoader.MockResourceLoader
import com.viceboy.data_repo.model.dataModel.Expense
import com.viceboy.data_repo.repository.ExpenseRepository
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@Suppress("UNCHECKED_CAST")
class MockExpenseRepository @Inject constructor(private val context: Context) : ExpenseRepository {

    private val mutableCachedMapOfExpense = HashMap<String, Expense>()
    private var mapOfExpenseId: LinkedTreeMap<String, Any>? = null

    override fun saveExpense(expense: Expense): Completable {
        return Completable.create {}
    }

    override fun remove(expense: Expense): Completable {
        return Completable.create {}
    }

    override fun loadExpenseParticipants(expenseId: String): Flowable<HashMap<String, Float>> {
        return Flowable.create({ emitter ->
            mapOfExpenseId?.let {
                MockResourceLoader.getModelData(
                    expenseId,
                    mutableCachedMapOfExpense,
                    it
                ).subscribe({ expense ->
                    emitter.onNext(expense.expenseSharedBy)
                }, {
                    emitter.onError(it)
                }, {
                    emitter.onComplete()
                })
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadExpense(expenseId: String): Flowable<Expense> {
        return Flowable.create({ emitter ->
            mapOfExpenseId?.let {
                MockResourceLoader.getModelData(
                    expenseId,
                    mutableCachedMapOfExpense,
                    it
                ).subscribe({ expense ->
                    emitter.onNext(expense)
                }, {
                    emitter.onError(it)
                }, {
                    emitter.onComplete()
                })
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadExpenseByGroupId(groupId: String): Flowable<List<Expense>> {
        return Flowable.create({ emitter ->
            mapOfExpenseId?.let { map ->
                val listOfExpense = MockResourceLoader.queryDataListWithParams<Expense>(
                    map,
                    "groupId",
                    groupId
                )
                if (listOfExpense.isNotEmpty())
                    emitter.onNext(listOfExpense)
                else
                    emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadExpenseByUserId(userId: String): Flowable<List<Expense>> {
        return Flowable.create({ emitter ->
            mapOfExpenseId?.let {
                val listOfExpense = MockResourceLoader.queryDataListWithParams<Expense>(
                    it,
                    "expenseOwner",
                    userId
                )
                if (listOfExpense.isNotEmpty())
                    emitter.onNext(listOfExpense)
                else
                    emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)
    }

    override fun loadLatestExpense(list: Array<Expense>): Flowable<Expense> {
        return Flowable.create({ emitter ->
            if (list.isNotEmpty()) {
                val latestExpense = list.maxBy { it.expenseDate }
                latestExpense?.let { emitter.onNext(it) }
            } else {
                emitter.onComplete()
            }
        }, BackpressureStrategy.LATEST)

    }

    override fun loadExpenseByYear(
        userId: String,
        year: Int
    ): Flowable<LinkedTreeMap<String, List<Expense>>> {
        return loadExpenseByUserId(userId)
            .map {
                val mapOfCurrency = LinkedTreeMap<String, List<Expense>>()
                val listOfCurrency = it.map { it.currency }.distinct()
                val listOfExpense = it.filter { expense ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = expense.expenseDate
                    calendar.get(Calendar.YEAR) == year
                }
                listOfCurrency.forEach { currency ->
                    val listOfExpenseByCurrency = listOfExpense.filter { it.currency == currency }
                    mapOfCurrency[currency] = listOfExpenseByCurrency
                }
                return@map mapOfCurrency
            }
    }

    private fun mapOfExpense(): LinkedTreeMap<String, Any> {
        val mapOfGroups = MockResourceLoader.getDummyResponseFrom(
            context,
            method,
            endPoint.split("/").toTypedArray()
        )
        return mapOfGroups?.get(NODE_EXPENSE) as LinkedTreeMap<String, Any>
    }

    init {
        mapOfExpenseId = mapOfExpense()
    }

    companion object {
        private const val NODE_EXPENSE = "expenses"
        private const val method = "users_expenses"
        private const val endPoint = "data/users_expenses"
    }
}